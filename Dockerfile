FROM adoptopenjdk/openjdk13:alpine-slim

LABEL maintainer="Grzegorz Malewicz"
LABEL github="https://github.com/gmalewicz/golf-app"

ARG APP=./target/golf-*.jar

RUN addgroup -S golf && adduser -S golf -G golf

USER golf:golf

RUN sudo mkdir /usr/golf

COPY ${APP} /usr/golf/golf.jar

WORKDIR /usr/golf

ENTRYPOINT ["java","-jar","golf.jar"]

EXPOSE 8080
