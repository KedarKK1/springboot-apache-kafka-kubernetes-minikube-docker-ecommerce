# Use the standard Temurin 21 image (supports both ARM64 and AMD64)
FROM eclipse-temurin:21-jre

WORKDIR /app

# Ensure you have run './mvnw clean package' before building
# This copies the generated jar file into the container
COPY target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]