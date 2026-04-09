FROM openjdk:8-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 3011
ENTRYPOINT ["java", "-jar", "app.jar"]
