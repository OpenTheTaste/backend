# 1단계: 빌드
FROM gradle:8.14-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle :apps:api-admin:bootJar --no-daemon

# 2단계: 실행
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/apps/api-admin/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]