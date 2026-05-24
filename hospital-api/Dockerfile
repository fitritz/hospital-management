FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /build

# Copy pom and sources and build the fat jar
COPY pom.xml ./
COPY src ./src
RUN mvn -DskipTests package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
EXPOSE 8080
ENV PORT 8080
CMD ["java", "-Dserver.port=${PORT}", "-jar", "/app/app.jar"]
