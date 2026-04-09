FROM openjdk:8-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 3015
ENTRYPOINT ["java", "-jar", "app.jar"]
