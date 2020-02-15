
# New

- rewrite pi iot client to write raw measure to kafka topic
    - define common package for all digitalpanda project (manage exchange objects & shared logic : backend, avro, etc..)
    - test

- setup kafka-connect with cassandra connector
    - setup cassandra tables
    - setup kafka connectors
    - test cassandra sink from avro topic
- write flink jobs
 - setup jar submit mechanism:
    - create hadoop-flink-client docker image
        - add latest digestion jar into docker image and publish to registry
    - submit job from latest docker image !
 - optional: find or implement ConfluentRegistryAvroSerializationSchema

    
- frontend as a single js file copied into docker image
- frontend environement variable dev, test, prod
- iot service stop


- Optional: fix network routing
    - optional: routing to 192.168.1.0/24 ips from dev laptop (hadoop edge node role)
- Optional: Tune YARN & HDFS
    - Optional: custom always-on YARN queue for flink
    - Optional: tune jvm memory allocation for HDFS & YARN
- Optional: setup new-cassandra to multi-node mode
    - transfer metrics from old cassandra to kafka topic
    - sink raw metrics to new-cassandra table
- Optional: run containers as panda-worker user (fix nuc disk access rights binding)
- Optional: setup docker containers network in bridge mode with manual hosts file "etc_hosts"
- Optional: setup network routing to reach stressed pandas via fanless1

# subnet routing
    https://serverfault.com/questions/593448/routing-between-two-subnets-using-a-linux-box-with-two-nics/593457
# Old
- update pi3-1 to new hypriot OS version OR troubleshoot docker registry
- fix datanode display httpAddress to host ip instead of localhost in namenode ui
- fix ressource and node managers logging => not to default location container-unreachable
- enhance hadoop-common parametrization
- fix dns reverse lookup + enable dfs.namenode.datanode.registration.ip-hostname-check


