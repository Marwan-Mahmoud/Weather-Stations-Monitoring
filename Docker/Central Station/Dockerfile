FROM openjdk:19-jdk-alpine

WORKDIR /app

COPY centralstation.jar /app
COPY avro.avsc /app

RUN apk add --upgrade gcompat

CMD ["java", "-jar", "centralstation.jar"]