# ---------- Build Stage ----------
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copy Gradle wrapper first (for better caching)
COPY gradlew .
COPY gradle ./gradle
RUN chmod +x ./gradlew

# Copy Gradle build files to cache dependencies
COPY build.gradle settings.gradle ./

# Pre-download dependencies (cache layer)
RUN ./gradlew dependencies --no-daemon

# Copy the rest of the project
COPY . .

# Build the application (skip tests)
RUN ./gradlew clean build -x test --no-daemon


# ---------- Runtime Stage ----------
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

# Copy the built jar as app.jar
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]