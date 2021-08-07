FROM arm32v7/openjdk:11

LABEL maintainer="SiropOps <Cyril.Boillat@gmail.com>"

ENV TZ=Europe/Zurich

ADD ./target/app.jar /app/

EXPOSE 8012

CMD ["java", "-Xmx512m", "-jar", "/app/app.jar"]