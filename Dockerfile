FROM java:8-jre

ENV FATJAR_FILE backend/target/backend-1.0-SNAPSHOT.jar

# Set the location of the verticles
ENV FATJAR_HOME /opt/backend

EXPOSE 8080

COPY $FATJAR_FILE $FATJAR_HOME/
# COPY config/docker.json $VERTICLE_HOME/


WORKDIR $FATJAR_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["java -jar backend-1.0-SNAPSHOT.jar"]
