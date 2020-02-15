ThisBuild / resolvers ++= Seq(
    "Apache Development Snapshot Repository" at "https://repository.apache.org/content/repositories/snapshots/",
    "Maven Central" at "https://repo1.maven.org/maven2/",
    Resolver.mavenLocal
)

name := "sensor-digestion-flink"

version := "0.1-SNAPSHOT"

organization := "org.digitalpanda.wordcount"

ThisBuild / scalaVersion := "2.11.12" //Use 2.11.0 (Java 8) to be compatible with Hadoop 2.8.3; 2.11.12 => (Java 11), force java 11

val flinkVersion = "1.9.1"
val avroVersion = "1.9.1"
val beamVersion = "2.16.0"
val scalatestVersion = ""
val digitalpandaBackendApiVersion = "0.1.0"

val flinkDependencies = Seq(
  "org.apache.flink" %% "flink-scala" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion % "provided",

  "org.apache.flink" %% "flink-connector-kafka" % flinkVersion,
  "org.apache.flink" %% "flink-test-utils" % flinkVersion % "test",
  "org.apache.flink" %% "flink-runtime" % flinkVersion % "test" classifier "tests",
  "org.apache.flink" %% "flink-streaming-java" % flinkVersion % "test" classifier "tests",
  "org.scalatest" %% "scalatest" % "3.2.0-M1" % "test",

  "org.apache.flink" % "flink-avro" % flinkVersion,
  "org.apache.flink" % "flink-avro-confluent-registry" % flinkVersion,
  "org.apache.avro" % "avro" % avroVersion,
  "org.apache.beam" % "beam-sdks-java-core" % beamVersion,

  "org.digitalpanda" % "digitalpanda-common" % digitalpandaBackendApiVersion
)

lazy val root = (project in file(".")).
  settings(
    libraryDependencies ++= flinkDependencies
  )

assembly / mainClass := Some("org.digitalpanda.flink.sensor.digestion.DigestionJob")

// make run command include the provided dependencies
Compile / run  := Defaults.runTask(Compile / fullClasspath,
                                   Compile / run / mainClass,
                                   Compile / run / runner
                                  ).evaluated

// stays inside the sbt console when we press "ctrl-c" while a Flink programme executes with "run" or "runMain"
Compile / run / fork := true
Global / cancelable := true

// exclude Scala library from assembly
assembly / assemblyOption  := (assembly / assemblyOption).value.copy(includeScala = false)
