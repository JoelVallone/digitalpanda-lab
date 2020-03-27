package org.digitalpanda.iot.actors.measure.source

import akka.actor.{ActorRef, Props}
import com.datastax.driver.core.{Cluster, Session}
import org.digitalpanda.iot._
import org.digitalpanda.iot.actors.measure.source.CassandraDbSource.getCassandraDbSession

import scala.concurrent.duration.FiniteDuration

object CassandraDbSource {

  def props(cassandraContactPoint: String, cassandraPort : Int,
            samplingPeriod: FiniteDuration,
            displayPanel: ActorRef) : Props =
    Props(
      new CassandraDbSource(
        cassandraContactPoint,
        cassandraPort,
        samplingPeriod,
        displayPanel)
    )

  def getCassandraDbSession(contactPoint: String, port: Int) : Session =
    Cluster.builder()
      .addContactPoint(contactPoint)
      .withPort(port)
      .build()
      .connect("iot")
}

class CassandraDbSource(cassandraContactPoint: String,
                        cassandraPort : Int,
                        samplingPeriod: FiniteDuration,
                        displayPanel: ActorRef) extends SourceActor(samplingPeriod, List.empty, displayPanel) {

  private var dbSession = getCassandraDbSession(cassandraContactPoint, cassandraPort);

  override def sampleNewMeasures(requestId: Long): NewMeasures = {
    try {
      import collection.JavaConverters._
      val measuresMap = dbSession
        .execute("SELECT * from sensor_measure_latest")
        .asScala
        .map( row => {
          log.debug(row.toString)
          (row.getString("location"), MeasureType.withName(row.getString("measure_type"))) -> (row.getTimestamp("timestamp").getTime, row.getDouble("value"))
        })
        .toMap
      NewMeasures(requestId, measuresMap)
    } catch {
      case e: Throwable =>
        log.error( s"Connection error to Cassandra")
        log.warning(s"Trying to establish session for next request...")
        dbSession = getCassandraDbSession(cassandraContactPoint, cassandraPort)
        throw e
    }
  }

  override def postStop() : Unit = {
    log.info(s"Shutdown Casandra db source - begin")
    dbSession.close()
    log.info(s"Shutdown Casandra db source - end")
  }

}
