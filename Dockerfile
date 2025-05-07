# ------------ STAGE 1: Download Dependencies -------------
FROM maven:3.9-eclipse-temurin-17-alpine AS dependencies
WORKDIR /app
COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY orders-service/pom.xml orders-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml
COPY rabbitmq-client/pom.xml rabbitmq-client/pom.xml
COPY book-service/pom.xml book-service/pom.xml
# Add any other modules' pom.xml files here...
#RUN mvn -B dependency:resolve-plugins dependency:resolve
RUN mvn -pl eureka-server -am -B dependency:resolve-plugins dependency:resolve

# ------------ STAGE 2: Build the Application -------------
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app
COPY . .
RUN mvn -pl eureka-server -am clean package -DskipTests

# ------------ STAGE 3: Runtime --------------------------
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=builder /app/eureka-server/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]

#docker build -t eureka-server-image .