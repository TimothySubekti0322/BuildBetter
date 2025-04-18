# Use an official Maven image to build the application
FROM maven:3.9.9-amazoncorretto-23-al2023 AS build
WORKDIR /app

# Copy project files
COPY pom.xml .
COPY src ./src

# Package the application (skip tests if desired)
RUN mvn clean package -DskipTests

# List the contents of the target directory to verify the JAR file
RUN ls -l /app/target

# --- Stage 2: Create a minimal runtime container using Amazon Corretto ---
FROM amazoncorretto:23.0.2-al2023
WORKDIR /app

# Copy the generated JAR from the build stage
COPY --from=build /app/target/buildbetter-backend-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8080

# Define the entrypoint to run the JAR
CMD ["java", "-jar", "app.jar"]
