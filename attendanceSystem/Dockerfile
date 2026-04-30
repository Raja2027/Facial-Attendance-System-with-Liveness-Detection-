# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src
# Build the application skipping tests to speed up the process
# Limit Maven memory extremely aggressively to prevent OOM on 512MB free tier
ENV MAVEN_OPTS="-Xmx128m -XX:MaxMetaspaceSize=128m -XX:+TieredCompilation -XX:TieredStopAtLevel=1"
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
# Limit Java runtime memory strictly below 512MB to prevent Render OOM
ENTRYPOINT ["java", "-Xmx256m", "-XX:MaxMetaspaceSize=128m", "-Xss512k", "-XX:CICompilerCount=2", "-jar", "app.jar"]
