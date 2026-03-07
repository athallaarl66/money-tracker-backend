# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml dulu biar layer cache efisien
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source dan build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Run
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy jar dari stage build
COPY --from=builder /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Jalankan app
ENTRYPOINT ["java", "-jar", "app.jar"]