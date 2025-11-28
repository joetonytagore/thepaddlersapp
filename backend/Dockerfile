# Runtime Dockerfile: copy pre-built Spring Boot jar into a lightweight JRE image
FROM eclipse-temurin:17-jre as runtime
WORKDIR /app

# Copy the already-built jar from the build output
COPY build/libs/backend-0.1.0.jar /app/app.jar

EXPOSE 8081

# Run in dev profile by default for local images so container can start without external Postgres
ENV SPRING_PROFILES_ACTIVE=dev

ENTRYPOINT ["java","-jar","/app/app.jar"]
