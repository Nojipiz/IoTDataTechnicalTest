FROM virtuslab/scala-cli:1.4.0 as build
WORKDIR /app
COPY Consumer.scala /app
RUN scala-cli --power package Consumer.scala -o consumer.jar --assembly

# Run stage
FROM openjdk:23-ea-21-bullseye 
ENV TOPIC_NAME "ip_data"
ENV BOOTSTRAP_SERVERS "kafka:9092"
ENV PERSISTENCE_SERVER "persistence"
RUN mkdir /app
COPY --from=build /app/* /app
WORKDIR /app
CMD java -jar consumer.jar
