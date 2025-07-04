FROM openjdk:22-jdk-slim
VOLUME /tmp
COPY /target/metal-investment-0.0.1.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
