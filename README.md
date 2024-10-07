# Instructions
This project is created with Scala's versions 3.4.1 / 2.13.14 and JVM 21, it's not necesary to have these tools due containerization on the Part2 of the test, but those are a requiremente to run the Part1.
It was created using scala-cli so passing the Scala and JVM versions as parameters should be enough to run the script. 

## Running Part 1.

For the first part, the only thing you need is [scala-cli] (https://scala-cli.virtuslab.org/), and then run it with the following command

``` bash
scala-cli part1/Part1.scala
```

if there are some issues related to versions you can try using:
``` bash
scala-cli --jvm 21 part1/Part1.scala
```

## Running Part 2.

This part is entirely containerized, you should have docker and docker-compose to be able to run the project.
``` bash
docker-compose up
```

This command will create the producer, consumers, Kafka, Zookeper and Dragonfly containers, and will execute the mock data push into the **Kafka topic 'ip_data'**, by default it will be created with 4 partitions.

Once everything is created and running, every node will print into the terminal the amount of unique IPs that they recognize from the broker, this is an example of how it looks:
``` bash
consumer_one-1    |
consumer_one-1    |  ------------- Unique IPs -------------
consumer_one-1    |        Amount of unique IP's 300
consumer_one-1    |     at: Mon Oct 07 04:58:13 UTC 2024
consumer_one-1    |  --------------------------------------
consumer_one-1    |
consumer_two-1    |
consumer_two-1    |  ------------- Unique IPs -------------
consumer_two-1    |        Amount of unique IP's 300
consumer_two-1    |     at: Mon Oct 07 04:58:13 UTC 2024
consumer_two-1    |  --------------------------------------
consumer_two-1    |
consumer_three-1  |
consumer_three-1  |  ------------- Unique IPs -------------
consumer_three-1  |        Amount of unique IP's 300
consumer_three-1  |     at: Mon Oct 07 04:58:13 UTC 2024
consumer_three-1  |  --------------------------------------
consumer_three-1  |
```
