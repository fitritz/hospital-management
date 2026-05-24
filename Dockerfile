FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /build

# Copy only the hospital-api module to keep build context small
COPY hospital-api/pom.xml ./hospital-api/pom.xml
COPY hospital-api/src ./hospital-api/src
WORKDIR /build/hospital-api
RUN mvn -DskipTests package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /build/hospital-api/target/*.jar app.jar
EXPOSE 8080
ENV PORT 8080
CMD ["sh", "-c", "exec java -Dserver.port=$PORT -jar /app/app.jar"]
