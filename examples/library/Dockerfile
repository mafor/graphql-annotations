FROM openjdk:11
COPY build/libs/library.jar /app/app.jar
WORKDIR /app
ENTRYPOINT ["java", "-jar", "app.jar"]