FROM maven:3.8.3-openjdk-17

COPY . .

RUN mvn clean package

CMD ["java", "-jar", "target/prices.jar"]
