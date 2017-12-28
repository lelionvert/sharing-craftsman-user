FROM openjdk:8-jdk-alpine

RUN apk add --no-cache bash
RUN apk add --no-cache vim

VOLUME /tmp
ADD user.jar user.jar

ENV JAVA_OPTS=""

ENV SPRING_ACTIVE_PROFILE=<PROFILE>

ENV DATABASE_HOST=<DB_HOST>
ENV DATABASE_USER=<DB_USER>
ENV DATABASE_PASSWORD=<DB_PASSWORD>
ENV DATABASE_PORT=<DB_PORT>
ENV DATABASE_NAME=<DB_NAME>

EXPOSE 8080

ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /user.jar
