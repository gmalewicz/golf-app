FROM eclipse-temurin:17.0.4.1_1-jre-alpine

LABEL maintainer="Grzegorz Malewicz"
LABEL github="https://github.com/gmalewicz/golf-app"

ARG APP=./target/golf*.jar

RUN addgroup --system golf

RUN adduser --system golf

RUN adduser golf golf

RUN mkdir -p /home/grzegorz_malewicz/logs

RUN chmod 777 /home/grzegorz_malewicz/logs

USER golf:golf

WORKDIR /opt/golf

COPY ${APP} golf.jar

ENTRYPOINT ["java","-Dliquibase.hub.mode=off", "-Dhibernate.types.print.banner=false", "-jar","golf.jar"]

EXPOSE 8080
