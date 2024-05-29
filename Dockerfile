FROM openjdk:17-jdk-alpine
COPY target/demo-0.0.1-SNAPSHOT.jar coursach.jar
ENTRYPOINT ["java","-jar","coursach.jar"]