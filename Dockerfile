# ===== STAGE 1: Build the JAR using Maven =====
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# Copy all files and build the app
COPY . .
RUN mvn clean package -DskipTests

# ===== STAGE 2: Run the built JAR using JDK =====
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy only the built JAR from the previous stage
COPY --from=build /app/target/tenant-billing-0.0.1-SNAPSHOT.jar Backendapp.jar

# Run the app
ENTRYPOINT ["java", "-jar", "Backendapp.jar"]
