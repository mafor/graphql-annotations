FROM openjdk:11
COPY build/libs/GraphQLPlayground-0.0.1-SNAPSHOT.jar /app/app.jar
WORKDIR /app
CMD ["java", "-jar", "app.jar"]