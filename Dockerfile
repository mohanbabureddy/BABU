# Use OpenJDK 17 base image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy built jar (replace with your jar name if different)
COPY target/tenant-billing-0.0.1-SNAPSHOT.jar Backendapp.jar

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]
