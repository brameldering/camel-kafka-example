# Camel Kafka example

## Setup for use with Kafka on Kubernetes
Create a temporary NodePort to access the Kafka cluster from Orbital for testing purposes:

### Patch ClusterIP to NodePort
kubectl patch svc kafka-service-1 -n kafka --type=merge -p '{\"spec\": {\"type\": \"NodePort\"}}'

kubectl get svc kafka-service-1 -n kafka

### Undo the patch:
kubectl patch svc kafka-service-1 -n kafka --type=merge -p '{\"spec\": {\"type\": \"ClusterIP\"}}'

### To find the pod name for kafka:
kubectl get pods -n kafka

### To find the version of kafka in the pod:
kubectl exec -n kafka kafka-broker-......  -- kafka-topics.sh --version

Create Topics with partitions as specified in the readme for camel-example-kafka

### Open shell in kafka pod:
kubectl exec -it kafka-broker-1-66c68b4cdb-75tsp -n kafka -- bash

kafka-topics.sh --create \
  --zookeeper zookeeper-service.kafka.svc.cluster.local:2181 \
  --replication-factor 1 \
  --partitions 2 \
  --topic WebLogs

kafka-topics.sh --create \
  --zookeeper zookeeper-service.kafka.svc.cluster.local:2181 \
  --replication-factor 1 \
  --partitions 1 \
  --topic AccessLog

### To use Offset Explorer tool:
add to C:\Windows\System32\drivers\etc\hosts: 
127.0.0.1 kafka-service-1.kafka.svc.cluster.local
and in command:
kubectl port-forward svc/kafka-service-1 9092:9092 -n kafka

=======================================================================

### Introduction

An example which shows how to integrate Camel with Kakfa.

This example requires that Kafka Server is up and running.

You will need to create following topics before you run the examples.

1. bin\windows\kafka-topics.bat --create --zookeeper <zookeeper host ip>:<port> --replication-factor 1 --partitions 2 --topic WebLogs

2. bin\windows\kafka-topics.bat --create --zookeeper <zookeeper host ip>:<port> --replication-factor 1 --partitions 1 --topic AccessLog

This project consists of the following examples:


	1. Send messages continuously by typing on the command line.
	2. Example of partitioner for a given producer.
	3. Topic is sent in the header as well as in the URL.



### Build

You will need to compile this example first:

	mvn compile

### Run

1. Run the consumer first in separate shell 

	mvn compile exec:java -Pkafka-consumer


2. Run the message producer in the seperate shell

	mvn compile exec:java -Pkafka-producer

   Initially, some messages are sent programmatically. 
   
   On the command prompt, type the messages. Each line is sent as one message to kafka
   
   Type Ctrl-C to exit.



### Configuration

You can configure the details in the file:
  `src/main/resources/application.properties`



### Forum, Help, etc

If you hit an problems please let us know on the Camel Forums
	<http://camel.apache.org/discussion-forums.html>

Please help us make Apache Camel better - we appreciate any feedback you may
have.  Enjoy!



The Camel riders!
