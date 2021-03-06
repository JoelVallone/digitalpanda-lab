package org.digitalpanda.flink.sensor.digestion.operators

import java.time.Instant

import org.apache.flink.api.java.tuple.Tuple
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector
import org.digitalpanda.common.data.avro.Measure
import org.digitalpanda.common.data.history.{HistoricalDataStorageHelper, HistoricalDataStorageSizing}
case class EmitAggregateMeasure() extends ProcessWindowFunction[Measure, (String, Measure), String, TimeWindow] {

  def process(key: String, context: Context, aggregate: Iterable[Measure], out: Collector[(String, Measure)]): Unit = {
    val sampleTimestamp = middleWindowTimestamp(context);
    val aggregateMeasure = aggregate.iterator.next()
    out.collect((
      key,
      Measure
        .newBuilder(aggregateMeasure)
        .setTimeBlockId(timeBlockIdFrom(context, sampleTimestamp))
        .setTimestamp(sampleTimestamp)
        .build()
    ))
  }

  private def timeBlockIdFrom(context: Context, sampleTimestamp: Instant): Long =
    HistoricalDataStorageHelper
      .getHistoricalMeasureBlockId(
        middleWindowTimestamp(context).toEpochMilli,
        HistoricalDataStorageSizing.fromIntervalSeconds(windowSizeMillis(context) / 1000))

  private def middleWindowTimestamp(context: Context): Instant =
    Instant.ofEpochMilli(context.window.getStart + windowSizeMillis(context) / 2L)

  private def windowSizeMillis(context: Context) : Long =
    context.window.getEnd - context.window.getStart

  private def asString(tuple: Tuple): String =
    (0 until tuple.getArity)
      .map(id => tuple.getField(id).toString)
      .mkString("-")
}
