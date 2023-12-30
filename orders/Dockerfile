
FROM openjdk:17-jdk-alpine
ENV JWT_KEY asdkasdjfhghgsdfkajsdhfgakjsdhgfkajslksadflkasjhgklasjfdhg
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080
RUN java -version
ENTRYPOINT ["java","-jar","/app/app.jar"]
