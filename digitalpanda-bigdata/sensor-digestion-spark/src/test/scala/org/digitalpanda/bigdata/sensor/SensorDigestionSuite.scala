package org.digitalpanda.bigdata.sensor


import com.datastax.driver.core.Cluster
import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector.embedded.{EmbeddedCassandra, SparkTemplate, YamlTransformations}
import org.apache.spark.sql.SparkSession
import org.digitalpanda.backend.data.SensorMeasureType
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.digitalpanda.backend.data.SensorMeasureType._
import org.digitalpanda.backend.data.history.{HistoricalDataStorageHelper, HistoricalDataStorageSizing}
import org.digitalpanda.backend.data.history.HistoricalDataStorageSizing._
import org.digitalpanda.bigdata.sensor.SensorDigestion.parseDate

import scala.io.Source

@RunWith(classOf[JUnitRunner])
class SensorDigestionSuite extends FunSuite with BeforeAndAfterAll with EmbeddedCassandra with SparkTemplate {

  override def clearCache(): Unit = CassandraConnector.evictCache()

  //Sets up CassandraConfig and SparkContext
  useCassandraConfig(Seq(YamlTransformations.Default))

  val spark: SparkSession = useSparkConf(defaultConf)
  val connector = CassandraConnector(defaultConf)
  initEmbeddedDb(connector)

  var uut : SensorDigestion = new SensorDigestion(spark)

  override def afterAll(): Unit = {
    spark.stop()
  }

  def initEmbeddedDb(connector: CassandraConnector): Unit = {
    val source = Source.fromURL(getClass.getClassLoader.getResource("init.cql"))
    val initCql = source
      .getLines.mkString.split(";")
      .map(s => s + ";")
      .toList
    connector.withSessionDo( session => initCql.foreach(session.execute))
    source.close()
  }

  test("Should be able to access Embedded Cassandra Node") {
    assert(connector
      .withSessionDo(session => session.execute("SELECT * FROM system_schema.tables"))
      .all().toString.contains("system_schema"))
  }

  test("'loadLocatedMeasures'  loads set from Cassandra embedded DB ") {
    // Given
    val expected = Set(
      ("server-room",PRESSURE),
      ("server-room", TEMPERATURE),
      ("outdoor", PRESSURE),
      ("outdoor", TEMPERATURE),
      ("outdoor", HUMIDITY))

    // When
    val actual = uut.loadLocatedMeasures()

    // Then
    assert(actual === expected)
  }

  test("'loadSensorMeasure + avgAggregateHistory' for located measure over interval : load raw measures & compute aggregate  ") {
    // Given
    val location = "server-room"
    val metric = TEMPERATURE
    val beginDate =  parseDate("01/07/2019 00:00:00")
    val endDate =  parseDate("01/07/2019 00:20:00")
    val expected = Set(
      AnonymousMeasure(1561932570, 26.5),
      AnonymousMeasure(1561933170, 40.0)
    )

    // When
    val measures = uut.loadSensorMeasure(beginDate, endDate, location, metric).persist()
    val actual = uut.avgAggregateHistory(
      measures, beginDate, endDate, location, metric,
      SECOND_PRECISION_RAW, MINUTE_PRECISION_AVG)

    // Then
    assert(actual.collect().toSet === expected)
  }

  test("'saveAggregate' saves aggregate into appropriate target db table") {
    import spark.implicits._
    import collection.JavaConverters._

    // Given
    val cluster = Cluster.builder()
      .addContactPoint("127.0.0.1")
      .withPort(10550).build()
    val session = cluster.connect()
    val ds = List(
      AnonymousMeasure(1561932570, 26.5),
      AnonymousMeasure(1561933170, 40.0)
    ).toDS()

    // When
    val targetPrecision = HistoricalDataStorageSizing.MINUTE_PRECISION_AVG
    uut.saveAggregate(ds,
      "garage",
      SensorMeasureType.TEMPERATURE,
      targetPrecision)

    // Then
    val targetTable = s"iot.${HistoricalDataStorageHelper.cqlTableOf(targetPrecision)}"
    val actual = connector
      .withSessionDo(session => {
        session.execute(s"SELECT * FROM $targetTable")
      })
      .all().asScala
      .map(row => {
          Aggregate(
            row.getString("location"),
            row.getLong("time_block_id"),
            row.getString("measure_type"),
            row.getInt("bucket"),
            row.getTimestamp("timestamp").getTime,
            row.getDouble("value")
          )
        }
      )
    println(s"Data written to table $targetTable: $actual")
    assert(ds.collect().toSet === actual.map(a => AnonymousMeasure(a.timestamp, a.value)).toSet)
    assert(actual.head.location === "garage")
    assert(actual.head.measure_type === SensorMeasureType.TEMPERATURE.name())
  }
}