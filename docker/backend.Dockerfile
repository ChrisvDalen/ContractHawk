FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY backend/pom.xml pom.xml
COPY backend/src src
COPY contracts ../contracts
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -q -DskipTests package || \
    (apt-get update && apt-get install -y maven && mvn -B -q -DskipTests package)

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
