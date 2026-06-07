# ជំហានទី ១៖ Build កូដជាមួយ Maven
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# ជំហានទី ២៖ រត់កម្មវិធីជាមួយ Java 17
FROM openjdk:17-jdk-slim
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]