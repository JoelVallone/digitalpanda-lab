package org.digitalpanda.iot.actors.measure.source

import akka.actor.{ActorRef, Props}
import com.datastax.driver.core.{Cluster, Session}
import org.digitalpanda.iot._

import scala.concurrent.duration.FiniteDuration

object CassandraDbSource {

  def props(cassandraContactPoint: String, cassandraPort : Int,
            samplingPeriod: FiniteDuration,
            displayPanel: ActorRef) : Props =
    Props(
      new CassandraDbSource(
        getCassandraDbSession(cassandraContactPoint, cassandraPort),
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

class CassandraDbSource(dbSession: Session,
                        samplingPeriod: FiniteDuration,
                        displayPanel: ActorRef) extends SourceActor(samplingPeriod, List.empty, displayPanel) {

  override def sampleNewMeasures(requestId: Long): NewMeasures = {
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
  }

  override def postStop() : Unit = {
    log.info(s"Shutdown Casandra db source - begin")
    dbSession.close()
    log.info(s"Shutdown Casandra db source - end")
  }

}
