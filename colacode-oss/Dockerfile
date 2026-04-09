FROM openjdk:8-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 4000
ENTRYPOINT ["java", "-jar", "app.jar"]
