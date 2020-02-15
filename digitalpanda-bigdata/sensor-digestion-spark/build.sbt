name := "sensor-digestion-spark"

version := "0.1"

scalaVersion := "2.11.0" //Use 2.11.0 (Java 8 to be compatible with Hadoop 2.8.3; 2.11.12 => (Java 11)

resolvers += Resolver.mavenLocal

val sparkVersion = "2.4.3"
val connectorVersion = "2.4.1"
val scalaTestVersion = "2.2.4"
val jUnitVersion = "4.12"
val cassandraVersion = "3.2"
val cassandraClientVersion = "3.7.2"

libraryDependencies ++= Seq(
  // Scala
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "com.datastax.spark" %% "spark-cassandra-connector" % connectorVersion,

  // Java
  //"com.datastax.cassandra" % "cassandra-driver-mapping" % cassandraClientVersion, <= Do not use as it causes class clash
  "org.digitalpanda" % "digitalpanda-common" % "0.1.0"
)
/* TODO: Resolve dependency version issue with tests

  .map(_.excludeAll(
  ExclusionRule("com.google.guava","guava"))
) // Excluded to allow for Cassandra to run embedded
resolvers += "DataStax Repo" at "https://repo.datastax.com/public-repos/"*/

libraryDependencies ++= Seq(
  "com.datastax.spark"  %% "spark-cassandra-connector-embedded" % "2.0.10" % "test",
  "org.apache.cassandra" % "cassandra-all" % cassandraVersion % "test",
  "junit" % "junit" % jUnitVersion %  "test",
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test"

).map(_.excludeAll(
  ExclusionRule("org.slf4j", "log4j-over-slf4j"),
  ExclusionRule("org.slf4j", "slf4j-log4j12"),
  ExclusionRule("net.jpountz.lz4", "lz4"))
) // Excluded to allow for Cassandra to run embedded

//Forking is required for the Embedded Cassandra
fork in Test := true

