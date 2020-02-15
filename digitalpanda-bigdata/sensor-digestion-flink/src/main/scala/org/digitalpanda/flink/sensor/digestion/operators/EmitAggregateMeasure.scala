package org.digitalpanda.flink.sensor.digestion.operators

import java.time.Instant

import org.apache.flink.api.java.tuple.Tuple
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector
import org.digitalpanda.avro.Measure
import org.digitalpanda.backend.data.history.{HistoricalDataStorageHelper, HistoricalDataStorageSizing}

case class EmitAggregateMeasure() extends ProcessWindowFunction[Double, (String, Measure), Tuple, TimeWindow] {

  def process(key: Tuple, context: Context, aggregate: Iterable[Double], out: Collector[(String, Measure)]): Unit = {
    val sampleTimestamp = middleWindowTimestamp(context);
    out.collect((
      asString(key),
      Measure
        .newBuilder()
        .setLocation(key.getField(0))
        .setTimeBlockId(timeBlockIdFrom(context, sampleTimestamp))
        .setMeasureType(key.getField(1))
        .setTimestamp(sampleTimestamp)
        .setValue(aggregate.iterator.next())
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
