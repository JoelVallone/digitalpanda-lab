package org.digitalpanda

import org.digitalpanda.iot.MeasureType.MeasureType

package object iot {

  type Location = String
  type Timestamp = Long
  type Measure = Double

  object MeasureType extends Enumeration {
    type MeasureType = Value
    val PRESSURE, TEMPERATURE, HUMIDITY = Value
  }

  case class NewMeasures(requestId : Long, measures: Map[(Location, MeasureType), (Timestamp, Measure)])

  case class MeasureQuery(requestId : Long, targetMeasures : List[(Location, MeasureType)])
  case class MeasureQueryResponse(requestId : Long, measures: Map[(Location, MeasureType), (Timestamp, Measure)])
}
