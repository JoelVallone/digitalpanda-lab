package org.digitalpanda.flink.sensor.digestion.operators

import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor
import org.digitalpanda.avro.Measure
case class MeasureTimestampExtractor() extends AscendingTimestampExtractor[Measure] {

  withViolationHandler(new AscendingTimestampExtractor.IgnoringHandler())

  override def extractAscendingTimestamp(element: Measure): Long = element.getTimestamp.getEpochSecond* 1000L
}