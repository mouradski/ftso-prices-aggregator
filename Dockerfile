FROM ubuntu:20.04

RUN apt-get update --allow-unauthenticated
RUN apt-get install -y wget tar git
RUN apt-get clean
RUN rm -rf /var/lib/apt/lists/*


RUN wget https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21+35/OpenJDK21U-jdk_x64_linux_hotspot_21_35.tar.gz && \
    tar -xzf OpenJDK21U-jdk_x64_linux_hotspot_21_35.tar.gz && \
    mv jdk-21+35 /opt/java

ENV JAVA_HOME=/opt/java
ENV PATH="$JAVA_HOME/bin:$PATH"

ARG MAVEN_VERSION=3.8.8

RUN wget https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.zip && \
    apt-get update && \
    apt-get install -y unzip && \
    unzip apache-maven-$MAVEN_VERSION-bin.zip && \
    rm apache-maven-$MAVEN_VERSION-bin.zip && \
    mv apache-maven-$MAVEN_VERSION /opt/maven

ENV MAVEN_HOME=/opt/maven
ENV PATH="$MAVEN_HOME/bin:$PATH"


COPY . .

RUN git clone https://github.com/mouradski/ftso-ws-prices.git
WORKDIR /ftso-ws-prices
RUN mvn clean package -DskipTests


CMD ["sh", "-c", "java -jar target/quarkus-app/quarkus-run.jar"]
