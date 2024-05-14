FROM openjdk:21-jdk

WORKDIR /app

COPY centralstation.jar /app
COPY avro.avsc /app

CMD ["java", "-jar", "centralstation.jar"]