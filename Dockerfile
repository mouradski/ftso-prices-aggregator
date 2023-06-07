FROM maven:3.8.3-openjdk-17

COPY . .

RUN mvn clean package -DskipTests -Pserver

CMD ["java", "-jar", "target/ws-client.jar"]
