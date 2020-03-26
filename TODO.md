# New
## Jupyter-Lab on steroids
- Setup PiSpark within jupyter-lab
    - https://www.sicara.ai/blog/2017-05-02-get-started-pyspark-jupyter-notebook-3-minutes

# Up to date backend
- Deploy backend with up to date code (verify cassandra table access code)

## sensor-digestion-flink
 - Find or implement ConfluentRegistryAvroSerializationSchema
    - Wait flink 1.11 OR use third party lib

# Process all historical data
- create HistoricalDataPushJob (read from cassandra, write to kafka) with either Flink OR with Jupyter Lab & PiSpark
- Verify that results are processed and sinked into the cassandra tables


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


