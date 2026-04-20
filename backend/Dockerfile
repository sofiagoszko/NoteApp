FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copiar wrapper y pom primero (aprovecha caché de Docker)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# Copiar fuentes y compilar
COPY src src
RUN ./mvnw clean package -DskipTests -q


FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]