FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/tenant-billing-0.0.1-SNAPSHOT.jar Backendapp.jar
ENTRYPOINT ["java", "-jar", "Backendapp.jar"]
