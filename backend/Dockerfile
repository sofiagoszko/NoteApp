FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring && chown spring:spring /app
USER spring

COPY --chown=spring:spring mvnw .
COPY --chown=spring:spring .mvn .mvn
COPY --chown=spring:spring pom.xml .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

COPY --chown=spring:spring src src
RUN ./mvnw clean package -DskipTests -q


FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Correr la aplicación como usuario no-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]