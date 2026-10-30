# Arithmos Calculator — DevOps Pipeline Mini-Project

A small but production-style Calculator REST API in Java (Spring Boot),
wrapped in a fully automated DevOps pipeline: **Git/GitHub → Jenkins →
Docker → Docker Compose**. Push to `main` and the pipeline builds, tests,
packages, containerizes, pushes, and deploys with no manual steps.

## API

| Operation | Endpoint | Behaviour |
|---|---|---|
| Add | `GET /api/v1/add?a=&b=` | Returns `a + b` |
| Subtract | `GET /api/v1/subtract?a=&b=` | Returns `a - b` |
| Multiply | `GET /api/v1/multiply?a=&b=` | Returns `a * b` |
| Divide | `GET /api/v1/divide?a=&b=` | Returns `a / b`; `b=0` → HTTP 400 with error JSON |
| Health | `GET /actuator/health` | Spring Actuator health probe (UP/DOWN) |

Example response: `{ "operation": "add", "a": 2, "b": 3, "result": 5 }`

Invalid or missing parameters return HTTP 400 with a structured error body,
e.g. `{ "timestamp": "...", "status": 400, "error": "Bad Request",
"message": "Parameters 'a' and 'b' must be valid numbers", "path": "/api/v1/add" }`.

## Prerequisites

- Java 21, Maven 3.9+, Docker + Docker Compose v2, Jenkins LTS with
  Pipeline, Git, GitHub, JUnit, Mailer, and Docker plugins.

## Build, test, run

```bash
mvn clean package -DskipTests   # build executable JAR (tests run separately in CI)
mvn test                        # 14 tests: 7 service unit + 7 MockMvc endpoint tests
java -jar target/arithmos-calculator-*.jar
```

The app listens on port 8080 by default (`APP_PORT` env overrides it).

## Call the API

```bash
curl "http://localhost:8080/api/v1/add?a=2&b=3"
curl "http://localhost:8080/api/v1/divide?a=20&b=4"
curl "http://localhost:8080/api/v1/divide?a=5&b=0"   # HTTP 400 + error JSON
curl http://localhost:8080/actuator/health
```

## Docker

```bash
docker build -t kaushalrajpuwar/arithmos-calculator:latest .
docker run -p 8080:8080 kaushalrajpuwar/arithmos-calculator:latest
```

Multi-stage build: Maven/JDK 21 compiles the JAR, then a slim JRE Alpine
stage copies only the JAR. Runs as non-root user `app`, `EXPOSE 8080`.

## Docker Compose (app + Nginx reverse proxy)

```bash
docker compose up -d
curl "http://localhost:8082/api/v1/multiply?a=6&b=7"  # via Nginx proxy
curl http://localhost:8081/actuator/health             # direct to app
docker compose ps
docker compose down
```

- `calculator` on host port 8081 with `/actuator/health` health check,
  `unless-stopped` restart policy, and `APP_PORT` env support.
- `proxy` (Nginx) on host port 8082, starts only once the calculator is
  healthy (`service_healthy` condition).

## Jenkins CI/CD

Declarative pipeline in `Jenkinsfile` (Pipeline-from-SCM, branch `main`):

1. **Checkout** — pull latest code on trigger.
2. **Build** — `mvn clean package -DskipTests`.
3. **Test** — `mvn test`; publishes JUnit XML; fails the build on test failure.
4. **Docker Build** — tags the image with the Jenkins build number (plus `:latest`).
5. **Push** — pushes to Docker Hub using the Jenkins-managed
   `dockerhub-creds` credential (never hard-coded).
6. **Deploy** — `docker compose up -d`, then verifies health via
   `/actuator/health` and an end-to-end call through the Nginx proxy.

Triggers: GitHub webhook (`githubPush()`) with Poll SCM (`H/2 * * * *`) as
fallback. Email notification (success/failure) is sent on every build.
Re-running the pipeline is idempotent (`compose up -d` + health gate).

Create a Pipeline job pointing at this repository with script path
`Jenkinsfile`; no extra configuration needed beyond the `dockerhub-creds`
credential and an SMTP server for mail.

## Repository layout

```
arithmos-pipeline/
  src/main/java/...        # Spring Boot application
  src/test/java/...        # JUnit 5 + MockMvc tests
  pom.xml                  # Maven build (executable JAR)
  Dockerfile               # multi-stage build
  .dockerignore
  docker-compose.yml       # app + Nginx proxy
  nginx/nginx.conf         # reverse-proxy config
  Jenkinsfile              # declarative CI/CD pipeline
  report/                  # LaTeX report sources + PDF
  README.md
```
