package org.digitalpanda.iot.app

import java.util.concurrent.TimeUnit

import akka.Done
import akka.actor.{ActorSystem, CoordinatedShutdown}
import akka.util.Timeout
import com.pi4j.io.gpio.RaspiPin
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import org.digitalpanda.iot.MeasureType
import org.digitalpanda.iot.actors.measure.source.CassandraDbSource
import org.digitalpanda.iot.actors.panel.PanelActor
import org.digitalpanda.iot.raspberrypi.circuits.panel.{PanelController, PanelDisplay}

import scala.concurrent.duration.{Duration, FiniteDuration}

object PanelApp extends App {
  
  private val logger = Logger("PanelApp")
  val conf = ConfigFactory.load()

  logger.info(s"Init - start")
  logger.info(s"Config: $conf")

  logger.info("> Instantiate actors")

  val system: ActorSystem = ActorSystem("panelApp")

  logger.info(">> Display")
  val panelDisplayActor = system.actorOf(
    PanelActor.props(
      new PanelController(
        outdoorMetric = (conf.getString("panel.data.window.out.name"), MeasureType.TEMPERATURE),
        indoorMetric = (conf.getString("panel.data.window.in.name"), MeasureType.TEMPERATURE),
        PanelDisplay(
          windowRedDiodeId = RaspiPin.GPIO_29,
          windowGreenDiodeId = RaspiPin.GPIO_28),
        metricFreshnessDelayMillis = 3 * conf.getLong("panel.data.refresh-period-millis")
      ),
      displayRefreshPeriod = Duration.create(conf.getLong("panel.display.refresh-period-millis"), TimeUnit.MILLISECONDS)
    ),
    "displayActor")

  logger.info(">> Measure source from database")
  val dbSourceActor = system.actorOf(
    CassandraDbSource.props(
      conf.getString("cassandra.contactpoint"), conf.getInt("cassandra.port"),
      Duration.create(conf.getLong("panel.data.refresh-period-millis"), TimeUnit.MILLISECONDS),
      panelDisplayActor
    ),
    "dbMeasureSource")

  logger.info("> Register shutdown hook")
  CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseActorSystemTerminate, "Actors clean shutdown"){
    () => {
      import system.dispatcher
      implicit val timeout: Timeout = Timeout(FiniteDuration.apply(10, TimeUnit.SECONDS))
      logger.info("Shutting down actor system - begin")
      system.stop(panelDisplayActor)
      system.stop(dbSourceActor)
      system
        .terminate()
        .map(_ => {logger.info("> The end"); Done})
    }}

  logger.info("> Init - end")
  logger.info("> Waiting for OS signals")
}
