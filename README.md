# Arithmos Calculator: DevOps Pipeline Mini-Project

A small but production-style Calculator REST API in Java (Spring Boot),
wrapped in a fully automated DevOps pipeline: **Git/GitHub to Jenkins to
Docker to Docker Compose**. Push to `main` and the pipeline builds, tests,
packages, containerizes, pushes, and deploys with no manual steps.

**Links**

- GitHub repository: https://github.com/KaushalrajPuwar/arithmos-pipeline
- Docker Hub image (public): https://hub.docker.com/r/kaushalrajpuwar/arithmos-calculator
  - `docker pull kaushalrajpuwar/arithmos-calculator:latest`

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
mvn test                        # 15 tests: 8 service unit + 7 MockMvc endpoint tests
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

1. **Checkout**: fetch the triggered revision from GitHub.
2. **Build**: run `mvn clean package -DskipTests`.
3. **Test**: run `mvn test`, publish JUnit XML, and fail on test failures.
4. **Docker Build**: tag the image with the Jenkins build number and `:latest`.
5. **Push**: publish both tags using the Jenkins-managed `dockerhub-creds`
   credential, never a hard-coded secret.
6. **Deploy**: run `docker compose down --remove-orphans`, then
   `docker compose up -d`; verify `/actuator/health` and an API call through Nginx.

Trigger: GitHub push webhook (`githubPush()`) delivered to Jenkins through
the reserved ngrok domain at
`https://unstaffed-disperser-tweezers.ngrok-free.dev/github-webhook/`.
For a later demo, run `brew services start jenkins-lts`, then run
`ngrok http 8080 --url https://unstaffed-disperser-tweezers.ngrok-free.dev`
in a terminal and keep it open. GitHub delivers pushes automatically while
the tunnel is active. Email notifications for success and failure are sent
on every build. Re-running the pipeline is idempotent
(`compose down --remove-orphans` followed by `compose up -d` and a health gate).

The configured Jenkins Pipeline job uses this repository and `Jenkinsfile`.
Its Docker Hub credential is stored in Jenkins, and Gmail SMTP is configured
for build notifications.

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
