import sbtassembly.MergeStrategy

name := "digitalpanda-iot-display"

version := "1.0"

scalaVersion := "2.12.6"

lazy val akkaVersion = "2.6.0-M3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.datastax.cassandra" % "cassandra-driver-core" % "3.7.2",
  "com.pi4j" % "pi4j-core" % "1.2",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)
mainClass in (Compile, packageBin) := Some("org.digitalpanda.iot.app.PanelApp")

assemblyMergeStrategy in assembly := {
  case x if x.contains("io.netty.versions.properties") => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
