package org.digitalpanda.iot.actors.panel

import akka.actor.{Actor, ActorLogging, Props, Timers}
import org.digitalpanda.iot._
import org.digitalpanda.iot.raspberrypi.circuits.panel.PanelController

import scala.concurrent.duration.FiniteDuration

object PanelActor {

  case object MeasurePull
  case object PullNewMeasure
  case object PanelUpdate
  case object PanelClockTick

  def props(panelController: PanelController,
            displayRefreshPeriod: FiniteDuration) : Props =
    Props(new PanelActor(panelController, displayRefreshPeriod))
}

class PanelActor(panelController: PanelController,
                 displayRefreshPeriod: FiniteDuration)
        extends Actor with ActorLogging with Timers {
  import PanelActor._

  timers.startTimerWithFixedDelay(PanelUpdate, PanelClockTick, displayRefreshPeriod)

  override def receive: Receive = {
    case NewMeasures(_, measures) => panelController.setMeasures(measures)
    case PanelClockTick => panelController.clockTick()
  }

  override def postStop() : Unit = {
    log.info(s"Shutdown display panel - begin")
    panelController.shutdown()
    log.info(s"Shutdown display panel - end")
  }

}
