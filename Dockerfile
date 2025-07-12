# OpenJDK 기반 이미지
FROM eclipse-temurin:17-jdk

# 작업 디렉토리 설정
WORKDIR /app

# 빌드 결과물 복사 (Gradle 기준)
ARG JAR_FILE=build/libs/*SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]