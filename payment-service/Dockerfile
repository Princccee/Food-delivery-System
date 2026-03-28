# Stage 1: Build the application
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copy gradle wrapper and related files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x ./gradlew

# Copy the source code
COPY src src

# Build the application (skipping tests)
RUN ./gradlew build -x test --no-daemon -Dorg.gradle.jvmargs="-Xmx256m"

# Stage 2: Create the runtime image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the application port
EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]
