
# New
## digialpanda-iot-sensor
- rewrite pi iot client to write raw measure to kafka topic: test

## kafka-connect
- setup cassandra connectors
    - setup cassandra tables
    - setup kafka connectors
    - test cassandra sink from avro topic

## sensor-digestion-flink
 - fix scala & java versions according to flink (1.11 ?) & hadoop platform needs
 - setup jar submit mechanism:
    - create hadoop-flink-client docker image
        - add latest digestion jar into docker image and publish to registry
    - submit job from latest docker image !
 - optional: find or implement ConfluentRegistryAvroSerializationSchema

## Misc.
- Optional: fix network routing
    - optional: routing to 192.168.1.0/24 ips from dev laptop (hadoop edge node role)
    - https://serverfault.com/questions/593448/routing-between-two-subnets-using-a-linux-box-with-two-nics/593457
- Optional: Tune YARN & HDFS
    - Optional: custom always-on YARN queue for flink
    - Optional: tune jvm memory allocation for HDFS & YARN
- Optional: setup new-cassandra to multi-node mode
    - transfer metrics from old cassandra to kafka topic
    - sink raw metrics to new-cassandra table
- Optional: run containers as panda-worker user (fix nuc disk access rights binding)
- Optional: setup docker containers network in bridge mode with manual hosts file "etc_hosts"


