#/bin/bash

function remote_cmd {
  REMOTE="panda-config@fanless1"
  echo "--> ${REMOTE}> $1"
  ssh ${REMOTE} $1
}

function docker_run_kafka {
  DOCKER_RUN_CMD=$1
  REMOTE_CMD="sudo docker run --net=host --rm confluentinc/cp-kafka:5.3.1 ${DOCKER_RUN_CMD}"
  remote_cmd "${REMOTE_CMD}"
}

echo "CONFLUENT PLATFORM CHECKS - $(date) - BEGINNING"
echo ""
echo ""

echo "=== Zookeeper ==="
remote_cmd 'for ip in "192.168.1.1" "192.168.1.242" "192.168.1.241" ; do mode=$(echo stat | nc -q 1 $ip 2181 | grep "Mode"); echo "$ip => $mode"; done'
echo ""

echo "=== Kafka - brokers ==="
echo "-> Topic creation (if not exists)"
docker_run_kafka 'kafka-topics --zookeeper stressed-panda-1.lab.digitalpanda.org:2181 --create --topic bar --partitions 3 --replication-factor 2 --if-not-exists'
echo "-> Describe topic"
docker_run_kafka 'kafka-topics --describe --topic bar --zookeeper stressed-panda-1.lab.digitalpanda.org:2181'
echo "-> Produce message"
docker_run_kafka 'bash -c "date | kafka-console-producer --broker-list stressed-panda-1.lab.digitalpanda.org:9092,stressed-panda-2.lab.digitalpanda.org:9092 --topic bar && echo produced date now message"'
echo "-> Consume message"
docker_run_kafka 'kafka-console-consumer --bootstrap-server stressed-panda-1.lab.digitalpanda.org:9092 --topic bar --from-beginning --timeout-ms 5000 || true'
echo "Continuing checks ..."
echo ""

echo "=== Avro schema registry ==="
echo "-> Get schema subject lits"
curl -X GET "http://fanless1:18081/subjects"
echo -e "\n-> Create new schema with subject"
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    --data '{"schema": "{\"type\": \"string\"}"}' \
    "http://fanless1:18081/subjects/a-topic-key/versions"
echo -e "\n-> Get latest schema from subject"
curl -X GET "http://fanless1:18081/subjects/a-topic-key/versions/latest"
echo -e "\n\n"

echo "=== Kafka - connect cluster ==="
echo "-> Connect workers status"
echo "--> stressed-panda-1:"
curl -s -X GET http://192.168.0.241:8083
echo -e "\n--> stressed-panda-2:"
curl -s -X GET http://192.168.0.242:8083
echo -e "\n"

echo ""
echo ""
echo "CONFLUENT PLATFORM CHECKS - $(date) - END"