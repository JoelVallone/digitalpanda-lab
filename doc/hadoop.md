# Hadoop cluster
## General
Download: http://hadoop.apache.org/releases.html

Guide:
	- https://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-common/ClusterSetup.html
	- http://www.allprogrammingtutorials.com/tutorials/setting-up-hadoop-2-6-0-cluster.php

## YARN
capacity scheduler & queues:
 - concepts: https://blog.cloudera.com/yarn-capacity-scheduler
 - config: https://hadoop.apache.org/docs/current/hadoop-yarn/hadoop-yarn-site/CapacityScheduler.html

## Install - Docker:

1) Install java openjdk 8 :
   > sudo apt-get update; sudo apt-get install openjdk-8-jre
2) > export JAVA_HOME={path to java installation folder}
3) Create directories set with hadoop_user as owner
    a) hadoop binary code $HADOOP_BINARY_FOLDER
    b) hadoop intermediate $HADOOP_DATA_FOLDER
    c) configuration $HADOOP_CONF_DIR
4) > cd $HADOOP_INSTALLATION_FOLDER
5) > wget {url to hadoop binary}
6) > tar -zxf {haddop .tgz file} && rm {haddop .tgz file}
7) (opt) create hadoop process user {hadoop user}
8) (opt) specify HADOOP_PID_DIR and HADOOP_LOG_DIR in etc/hadoop/hadoop-env.sh to point to hadoop process user owned repositories
9) (opt) at startup, set >"HADOOP_PREFIX=/path/to/hadoop; export HADOOP_PREFIX" in /etc/profile.d with a script
10) Configure hadoop nodes *.xml files
   - Global > $HADOOP_CONF_DIR/core-site.xml
            - fs.defaultFS = hdfs://{NameNode network interface ip}:50050/
            - io.file.buffer.size = 131072
   -  HDFS > $HADOOP_CONF_DIR/hdfs-site.xml
            -  NameNode (master)
                -  dfs.namenode.name.dir = $HADOOP_DATA_FOLDER/hdfs/master/data
            - DataNode (slave)
                        -  dfs.datanode.data.dir = $HADOOP_DATA_FOLDER/hdfs/slave/data
   - YARN > $HADOOP_CONF_DIR/yarn-site.xml
            - ResourceManager (master)
                        - yarn.resourcemanager.hostname = {ResourceManager network interface ip  OR hostname}
            - NodeManager (slave)
                        - yarn.resourcemanager.hostname = {ResourceManager network interface ip  OR hostname}
                        - yarn.nodemanager.local-dirs = $HADOOP_DATA_FOLDER/yarn/slave/data
                        - yarn.nodemanager.logs-dirs = $HADOOP_DATA_FOLDER/yarn/slave/logs

## Start - Ansible
11) Hadoop cluster startup
   - HDFS
	- NameNode (master)
        	 - >(only first time) $HADOOP_BINARY_FOLDER/bin/hdfs namenode -format hadoop-dfs
             - >$HADOOP_BINARY_FOLDER/sbin/hadoop-daemon.sh start namenode
        - DataNode (slave)
             - >$HADOOP_BINARY_FOLDER/sbin/hadoop-daemon.sh start datanode
   - YARN
        - ResourceManager (master)
             - >$HADOOP_BINARY_FOLDER/sbin/yarn-daemon.sh start resourcemanager
        - NodeManager (slave)
             - >$HADOOP_BINARY_FOLDER/sbin/yarn-daemon.sh start nodemanager
12) Hadoop cluster stop => same as 11) but stop instead of start
