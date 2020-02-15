package org.digitalpanda.iot.raspberrypi.circuits.panel

import com.typesafe.scalalogging.Logger
import org.digitalpanda.iot.MeasureType.MeasureType
import org.digitalpanda.iot.{Location, Measure, Timestamp}

class PanelController(outdoorMetric : (Location, MeasureType),
                      indoorMetric : (Location, MeasureType),
                      panelDisplay: PanelDisplay,
                      metricFreshnessDelayMillis: Long) {

  private val logger = Logger(classOf[PanelController])

  val targetMeasures = List(outdoorMetric, indoorMetric)

  private var latestMeasures : Map[(Location, MeasureType), (Timestamp, Measure)] = Map()


  def setMeasures(measures: Map[(Location, MeasureType), (Timestamp, Measure)]): Unit =
    latestMeasures = measures

  def clockTick(): Unit = {
    updateWindowInfo()
    panelDisplay.applyStateToDisplay()
  }

  def shutdown(): Unit = {
    panelDisplay.shutdown()
    panelDisplay.applyStateToDisplay()
    logger.info("Display has been shut down")
  }

  def updateWindowInfo(): Unit = {
    val outdoorMeasure = latestMeasures.get(outdoorMetric)
    val indoorMeasure = latestMeasures.get(indoorMetric)
    val freshnessLowerBoundMillis = System.currentTimeMillis() - metricFreshnessDelayMillis
    logger.debug(s"Panel controller: outdoorMeasure=$outdoorMeasure, indoorMeasure=$indoorMeasure")
    if (
      outdoorMeasure.isDefined && indoorMeasure.isDefined
        && outdoorMeasure.get._1 > freshnessLowerBoundMillis
        && indoorMeasure.get._1 > freshnessLowerBoundMillis
    )
      if (outdoorMeasure.get._2 > indoorMeasure.get._2) {
        panelDisplay.targetOpenWindow(false)
      } else
        panelDisplay.targetOpenWindow(true)
    else
      panelDisplay.targetPanicWindowState()
  }

}
