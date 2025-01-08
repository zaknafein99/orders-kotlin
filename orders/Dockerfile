
FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080
RUN java -version
ENTRYPOINT ["java","-jar","/app/app.jar"]
