# -------- 1️⃣ BUILD STAGE --------
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# pom.xml va source fayllarni nusxalash
COPY pom.xml .
COPY src ./src

# Maven orqali build (testlarni o‘tkazmasdan)
RUN mvn clean package -DskipTests

# -------- 2️⃣ RUNTIME STAGE --------
FROM eclipse-temurin:21-jdk
WORKDIR /app

# 1-bosqichdan .jar ni olish
COPY --from=builder /app/target/EmployeeCheckingPlatform-*.jar app.jar

# 8080 portni ochamiz
EXPOSE 8080

# ENTRYPOINT — ilovani ishga tushirish
ENTRYPOINT ["java","-jar","app.jar"]
