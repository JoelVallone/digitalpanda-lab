
# Confluent comunity platform
https://docs.confluent.io/current/installation/installing_cp/index.html#installation
docker install: 
 - https://docs.confluent.io/current/installation/docker/installation/index.html#cp-docker-install
 - quick-start: https://docs.confluent.io/current/quickstart/ce-docker-quickstart.html
 - three node deployment (see test procedure): https://docs.confluent.io/3.3.0/installation/docker/docs/tutorials/clustered-deployment.html
 - docker install: https://docs.confluent.io/current/installation/docker/index.html
 - config reference: https://docs.confluent.io/current/installation/docker/config-reference.html
## Kafka message broker & zookeeper
k8s quick deploy: https://github.com/PharosProduction/tutorial-apache-kafka-cluster
state with docker: https://docs.confluent.io/current/installation/docker/operations/external-volumes.html#external-volumes
docker with confluent docker images:  https://medium.com/@robcowart/deploying-confluent-platform-kafka-oss-using-docker-39b65fa6809b

### zookeeper
image: https://github.com/confluentinc/cp-docker-images/tree/5.3.1-post/debian/zookeeper
#### Deployment and quick check
multi node: https://docs.confluent.io/current/zookeeper/deployment.html#multi-node-setup
```shell script
ansible-playbook -i cluster-inventory cluster-kafka.yml -e clear_state=true
sudo docker logs -f cp-zookeeper-0
for ip in '192.168.1.1' '192.168.1.242' '192.168.1.241' ; do mode=$(echo stat | nc -q 1 $ip 2181 | grep "Mode"); echo "$ip => $mode"; done
```
ZooKeeper Commands: The Four Letter Word : https://zookeeper.apache.org/doc/r3.3.3/zookeeperAdmin.html#sc_maintenance

### kafka-broker
ISR : In Sync Replica
config keys: https://docs.confluent.io/current/installation/configuration/broker-configs.html

#### Check if the broker started:
```shell script
sudo docker logs cp-kafka-broker-1 | grep started
```

#### Create topic: 
```shell script
sudo docker run --net=host --rm confluentinc/cp-kafka:5.3.1 \
  kafka-topics --zookeeper stressed-panda-1.lab.digitalpanda.org:2181 --create --topic bar --partitions 3 --replication-factor 2 --if-not-exists 

```

#### Describe topic: 
```shell script
sudo docker run --net=host --rm confluentinc/cp-kafka:5.3.1 \
  kafka-topics  --zookeeper stressed-panda-1.lab.digitalpanda.org:2181 --topic bar --describe
```

#### Delete topic:
Ask brokers to delete topics
```shell script
sudo docker run --net=host --rm confluentinc/cp-kafka:5.3.1 \
  kafka-topics  --zookeeper stressed-panda-1.lab.digitalpanda.org:2181 --topic bar --delete 
```

If did not succeed, remove from zookeeper
```shell script
sudo docker run -ti --net=host --rm confluentinc/cp-kafka:5.3.1 zookeeper-shell fanless1:2181<<
> get /brokers/topics/bar
> rmr /brokers/topics/bar
> rmr /admin/delete_topics/bar
```
For each broker ensure topic partition directories are removed:
```shell script
sudo rm -r  /home/panda-config/cp-kafka-broker/data/bar-*
```

#### Produce data from console:
```shell script
sudo docker run --net=host --rm confluentinc/cp-kafka:5.3.1 \
  bash -c "seq 42 | kafka-console-producer --broker-list stressed-panda-1.lab.digitalpanda.org:9092,stressed-panda-2.lab.digitalpanda.org:9092 --topic bar && echo 'Produced 42 messages.'"
```

#### Consume data from console:
```shell script
#Might take some time when it is the first time that the broker system gets a request from a consumer.
# => The brokers must create the topic __consumers_offset
sudo docker run --net=host --rm confluentinc/cp-kafka:5.3.1 \
  kafka-console-consumer --bootstrap-server stressed-panda-1.lab.digitalpanda.org:9092 --topic bar --from-beginning --max-messages 42
```

## Avro schema registry
general https://docs.confluent.io/current/schema-registry/installation/deployment.html
config keys: https://docs.confluent.io/current/schema-registry/installation/config.html
api calls examples for testing: https://docs.confluent.io/3.1.1/schema-registry/docs/intro.html

#### Check server up
```shell script
curl -X GET "http://fanless1:18081/subjects"
```

## Kafka connect
docker and connect: https://docs.confluent.io/5.0.0/installation/docker/docs/installation/connect-avro-jdbc.html
config keys: https://docs.confluent.io/current/connect/references/allconfigs.html

example setup user-guide: https://docs.confluent.io/current/connect/userguide.html
kafka connect sink for cassandra: 
 - doc: https://docs.lenses.io/connectors/sink/cassandra.html
 - jar: https://github.com/lensesio/stream-reactor/releases/download/1.2.3/kafka-connect-cassandra-1.2.3-2.1.0-all.tar.gz

kafka connect jdbc:
 - jar: http://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.37.tar.gz

### Container is healty:
```shell script
sudo docker logs cp-kafka-connect-1 | grep started
```
Example output:
[2016-08-25 19:18:38,517] INFO Kafka Connect started (org.apache.kafka.connect.runtime.Connect)
[2016-08-25 19:18:38,557] INFO Herder started (org.apache.kafka.connect.runtime.distributed.DistributedHerder)


### Create a connect jdbc source connector
curl -X POST \
  -H "Content-Type: application/json" \
  --data '{ "name": "quickstart-jdbc-source", "config": { "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector", "tasks.max": 1, "connection.url": "jdbc:mysql://127.0.0.1:3306/connect_test?user=root&password=confluent", "mode": "incrementing", "incrementing.column.name": "id", "timestamp.column.name": "modified", "topic.prefix": "quickstart-jdbc-", "poll.interval.ms": 1000 } }' \
  http://$CONNECT_HOST:28083/connectors
  
### Create a connect file sink connector
curl -X POST -H "Content-Type: application/json" \
    --data '{  "name": "quickstart-avro-file-sink",  "config": {    "connector.class": "org.apache.kafka.connect.file.FileStreamSinkConnector",    "tasks.max": "1",    "topics": "bar",    "file": "/tmp/kafka-connect/file-sink/bar.txt"  }}' \
    http://stressed-panda-1:8083/connectors
  
### Check connector status:
curl -s -X GET http://stressed-panda-1:8083/connectors/quickstart-avro-file-sink/status