FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

RUN addgroup -S -g 1001 spring && adduser -S -u 1001 -G spring spring && chown spring:spring /app
USER spring

COPY --chown=spring:spring pom.xml .
COPY --chown=spring:spring src src
RUN cp src/main/resources/application.properties.example src/main/resources/application.properties

RUN --mount=type=cache,target=/home/spring/.m2,uid=1001,gid=1001 \
    mvn -B -q package -Dmaven.test.skip=true \
    && java -Djarmode=tools -jar target/*.jar extract --layers --launcher --destination extracted


FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S -g 1001 spring && adduser -S -u 1001 -G spring spring
USER spring

COPY --from=build --chown=spring:spring /app/extracted/dependencies/ ./
COPY --from=build --chown=spring:spring /app/extracted/spring-boot-loader/ ./
COPY --from=build --chown=spring:spring /app/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=spring:spring /app/extracted/application/ ./

EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]