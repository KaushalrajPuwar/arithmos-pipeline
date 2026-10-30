# ---- Build stage: Maven + JDK 21 ----
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy POM first for better layer caching of dependencies
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Copy sources and build the executable JAR (tests skipped here; Jenkins runs them explicitly)
COPY src ./src
RUN mvn -q -DskipTests package

# ---- Runtime stage: slim JRE only ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root user for security best practice
RUN addgroup -S app && adduser -S app -G app

COPY --from=build /build/target/arithmos-calculator-*.jar app.jar
RUN chown app:app app.jar

EXPOSE 8080
USER app

ENTRYPOINT ["java", "-jar", "app.jar"]
