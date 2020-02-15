package org.digitalpanda.flink.sensor.digestion

import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.util.FiniteTestSource
import org.apache.flink.test.util.MiniClusterWithClientResource
import org.digitalpanda.avro.MeasureType.{PRESSURE, TEMPERATURE}
import org.digitalpanda.avro.{Measure, RawMeasure}
import org.digitalpanda.backend.data.history.HistoricalDataStorageSizing
import org.digitalpanda.flink.test.TestHelper.measure
import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable.ArrayBuffer

// https://ci.apache.org/projects/flink/flink-docs-release-1.9/dev/stream/testing.html#testing-flink-jobs
class MeasureDigestionJobTest extends AnyFlatSpec with Matchers with BeforeAndAfter {

    private val flinkCluster = new MiniClusterWithClientResource(new MiniClusterResourceConfiguration.Builder()
      .setNumberSlotsPerTaskManager(1)
      .setNumberTaskManagers(2)
      .build)

    private val measureSec = measure(HistoricalDataStorageSizing.SECOND_PRECISION_RAW)_
    private val measureMin = measure(HistoricalDataStorageSizing.MINUTE_PRECISION_AVG)_

    before {
        flinkCluster.before()
    }

    after {
        flinkCluster.after()
    }

    "rawMetricPreProcessor sub-topology" should "map to cassandra-sinkable Measure" in {
        // Given
        val env = StreamExecutionEnvironment.getExecutionEnvironment.enableCheckpointing
        env.setParallelism(2)

        val expectedSeq = Seq(
            measureSec("server-room",  TEMPERATURE, "2019-06-30T22:09:59Z", 26.0),
            measureSec("server-room",  PRESSURE,    "2019-06-30T22:10:20Z", 789.0)
        )
        val rawInput = expectedSeq
          .map(expected =>
              RawMeasure.newBuilder()
                  .setLocation(expected.getLocation)
                  .setMeasureType(expected.getMeasureType)
                  .setTimestamp(expected.getTimestamp)
                  .setValue(expected.getValue)
                  .build())
        val rawMetricSource = new FiniteTestSource(rawInput:_*)
        val metricSink = new CollectSink()
        CollectSink.values.clear()

        // When
        println(s"Raw input: \n${rawInput}\n")
        MeasureDigestionJob
          .rawMetricPreProcessor(env, rawMetricSource, metricSink)
          .execute("MeasureDigestionJobUUT")

        // Then
        println(s"Sinkable keyed output: \n${CollectSink.values.mkString("\n")}")
        CollectSink.values.toSet shouldEqual Set(
          ("server-room-TEMPERATURE", measureSec("server-room",  TEMPERATURE, "2019-06-30T22:09:59Z", 26.0)),
          ("server-room-PRESSURE", measureSec("server-room",  PRESSURE,    "2019-06-30T22:10:20Z", 789.0))
        )

    }

    "windowAverage sub-topology" should "compute 60 seconds averages by <Location, MeasureType>" in {
        // Given
        val env = StreamExecutionEnvironment.getExecutionEnvironment.enableCheckpointing
        env.setParallelism(2)

        val rawMeasures = Seq(
            measureSec("server-room",  TEMPERATURE, "2019-06-30T22:09:59Z", 26.0),

            measureSec("server-room",  TEMPERATURE, "2019-06-30T22:10:00Z", 35.5),
            measureSec("server-room",  TEMPERATURE, "2019-06-30T22:10:10Z", 30.0),
            measureSec("server-room",  PRESSURE,    "2019-06-30T22:10:20Z", 789.0),
            measureSec("server-room",  TEMPERATURE, "2019-06-30T22:10:20Z", 35.0),
            measureSec("outdoor",      TEMPERATURE, "2019-06-30T22:10:20Z", 5.0),
            measureSec("server-room",  TEMPERATURE, "2019-06-30T22:10:59Z", 30.0),

            measureSec("server-room",  TEMPERATURE,  "2019-06-30T22:12:00Z", 30.0),
            measureSec("server-room",  TEMPERATURE,  "2019-06-30T22:12:59Z", 54.0)
        )
        val metricInput = new FiniteTestSource(rawMeasures:_*)
        val avgOutput = new CollectSink()
        CollectSink.values.clear()

        // When
        println(s"Input at second-frequency: \n${rawMeasures.mkString("\n")}\n")
        MeasureDigestionJob
          .windowAverage(env, Time.seconds(60L), metricInput, avgOutput)
          .execute("MeasureDigestionJobUUT")

        // Then
        println(s"Output averages at minute frequency: \n${CollectSink.values.mkString("\n")}")
        CollectSink.values.toSet shouldEqual Set(
          ("server-room-TEMPERATURE", measureMin("server-room",  TEMPERATURE,"2019-06-30T22:09:30Z", 26.0)),
          ("server-room-TEMPERATURE", measureMin("server-room",  TEMPERATURE,"2019-06-30T22:10:30Z",32.625)),
          ("outdoor-TEMPERATURE",     measureMin("outdoor",      TEMPERATURE,"2019-06-30T22:10:30Z",5.0)),
          ("server-room-PRESSURE",    measureMin("server-room",  PRESSURE,   "2019-06-30T22:10:30Z",789.0)),
          ("server-room-TEMPERATURE", measureMin("server-room",  TEMPERATURE,"2019-06-30T22:12:30Z",42.0))
        )
    }
}

class CollectSink extends SinkFunction[(String, Measure)] {

  override def invoke(value: (String, Measure)): Unit = {
    synchronized {
      CollectSink.values.append(value)
    }
  }
}

object CollectSink {
  val values: ArrayBuffer[(String, Measure)] = ArrayBuffer()
}