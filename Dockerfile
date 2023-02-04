FROM adoptopenjdk/openjdk11
VOLUME /tmp
COPY github-webhook-listner-0.0.1.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]