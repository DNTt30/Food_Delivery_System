# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Package the application (skipping tests to speed up deployment)
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the generated JAR file from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
