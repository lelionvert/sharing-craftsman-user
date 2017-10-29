FROM openjdk:8-jdk-alpine

VOLUME /tmp
ADD target/user-1.0-SNAPSHOT.jar app.jar

ENV JAVA_OPTS=""

ENV SPRING_ACTIVE_PROFILE="dev"
ENV DATABASE_USER="root"
ENV DATABASE_PASSWORD="root"
ENV DATABASE_PORT=32775
ENV DATABASE_NAME="sharingcraftsmanuser"

EXPOSE 8080

ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar

