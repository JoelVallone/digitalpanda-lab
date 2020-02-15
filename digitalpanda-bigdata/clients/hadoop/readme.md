# Hadoop notes

## submit a test job to cluster

```shell script
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64
export HADOOP_VERSION=2.9.2
wget https://www-eu.apache.org/dist/hadoop/common/hadoop-${HADOOP_VERSION}/hadoop-${HADOOP_VERSION}.tar.gz
tar -zxf hadoop-${HADOOP_VERSION}.tar.gz
cd hadoop-${HADOOP_VERSION}/bin
export YARN_EXAMPLES=$(pwd)/../share/hadoop/mapreduce
./yarn jar \
  ${YARN_EXAMPLES}/hadoop-mapreduce-examples-${HADOOP_VERSION}.jar pi 16 1000
```