FROM maven:3.9-eclipse-temurin-11 AS build
WORKDIR /src
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=build /src/target/*.jar app.jar
EXPOSE 8080
ENV AUDIT_DB_PATH=/data/audit.db
ENTRYPOINT ["java","-jar","/app/app.jar"]
