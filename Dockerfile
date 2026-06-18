# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Cài đặt thư viện để decode base64 (Alpine linux cần cài thêm nếu thiếu, nhưng thường có sẵn base64)
# Script này sẽ kiểm tra biến FIREBASE_CONFIG_BASE64 và tạo file json
COPY entrypoint.sh .
RUN chmod +x entrypoint.sh

EXPOSE 8080
ENTRYPOINT ["./entrypoint.sh"]