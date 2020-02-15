package org.digitalpanda.iot.actors.measure.aggregator

import akka.actor.{Actor, ActorLogging, Props}
import org.digitalpanda.iot.MeasureType.MeasureType
import org.digitalpanda.iot._

object LatestMeasureAggregator  {
  def props(): Props = Props(new LatestMeasureAggregator())
}

class LatestMeasureAggregator extends Actor with ActorLogging {

  private var measureMap : Map[(Location, MeasureType), (Timestamp, Measure)] = Map()

  override def receive: Receive = {
    case NewMeasures(_, newMeasures) =>
      measureMap = measureMap ++ newMeasures

    case MeasureQuery(requestId, targetMeasures) =>
      sender() !
        MeasureQueryResponse(
          requestId,
          measureMap.filter{ case (key,_) => targetMeasures.contains(key)}
        )
  }
}
