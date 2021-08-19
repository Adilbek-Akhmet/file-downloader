FROM openjdk:11

EXPOSE 8081

WORKDIR /usr/src/app/fileDownload

RUN mkdir "storage"
RUN mkdir "logs"

ARG JAR_FILE=target/FileDownload-0.0.1-SNAPSHOT.jar

COPY target/FileDownload-0.0.1-SNAPSHOT.jar /usr/src/app/app.jar

ENTRYPOINT ["java", "-jar", "/usr/src/app/app.jar"]
