BigData Assignment 3
--------------------------------------
Team Members
Kavya Jampani (Net id: kxj170004)
Hari Chandana Epuri (Net id: hxe17000)
---------------------------------------

Question 1:
---------------------------------------
1. Import the Kafka project to IntelliJ IDEA
2. Open bash and give sbt command.
3. Then, type assembly to build the fat jar. (sbt:kafka> assembly)
4. Start the zookeeper using the following command
.\bin\windows\zookeeper-server-start.bat config\zookeeper.properties
5. Start the kafka using the following command
.\bin\windows\kafka-server-start.bat config\server.properties
6. Create the topic using the command
.bin\windows\kafka-topics.bat --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic topicA
7. Start the consumer using the command
.bin\windows\kafka-console-producer.bat --broker-list localhost:9092 --topic topicB
8. Run the Spark project from Intellij with program argument as "topicA" or using the following command
>spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.0 --class KafkaExample Pathtojarfile topicA <path to folder "a">
9. The sentiments starts to appear in consumer console
10. Launch Elastic search, kibana and Logstash to view the visualization dashboard.
12. Add the logstash-simple.conf file in the Logstash directory with the following content.

input {
kafka {
bootstrap_servers => "localhost:9092"
topics => ["topicB"]
}
}
output {
elasticsearch {
hosts => ["localhost:9200"]
index => "topic-b-index"
}
}


Question 2
------------------------------------------------------------
Import the kafka project folder into Intellij Idea
Run the program wikiGraphX with program arguments <inputPath> <outputDirectory>
(or)
Run the Spark project using the command
>spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.0 --class wikiGraphX Pathtojarfile <inputPath> <outputDirectory> <path to folder "a">

The last argument in both of the programs is to set the hadoop.home.dir system property.