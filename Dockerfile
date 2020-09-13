FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar",\
"-Dmail.from=**********************",\
"-Dmail.password=**********************",\
"-Dmail.port=**********************",\
"-Dmail.host=**********************",\
"-Dencoder.secrete=**********************"]

