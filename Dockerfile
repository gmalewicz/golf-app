FROM adoptopenjdk/openjdk13:alpine-slim

LABEL maintainer="Grzegorz Malewicz"
LABEL github="https://github.com/gmalewicz/golf-app"

ARG APP=./target/golf-*.jar

RUN addgroup -S golf && adduser -S golf -G golf

RUN mkdir -p /home/grzegorz_malewicz/logs

USER golf:golf

WORKDIR /opt/golf

COPY ${APP} golf.jar

ENTRYPOINT ["java","-jar","golf.jar"]

EXPOSE 8080
