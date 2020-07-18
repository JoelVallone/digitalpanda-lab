package org.digitalpanda.flink.sensor.digestion.operators

import java.time.Instant

import org.apache.flink.api.common.functions.AggregateFunction
import org.digitalpanda.common.data.avro.{Measure, MeasureType}

case class AverageMeasureAggregate() extends AggregateFunction[Measure, (Measure, Double, Long), Measure] {

  override def createAccumulator(): (Measure, Double, Long) = (emptyMeasure(), 0.0, 0L)

  override def add(measure: Measure, acc: (Measure, Double, Long)): (Measure, Double, Long) =
    (measure, acc._2 + measure.getValue, acc._3 + 1L)

  override def getResult(acc: (Measure, Double, Long)): Measure =
    Measure.newBuilder(acc._1).setValue(acc._2 / acc._3).build()

  override def merge(a: (Measure, Double, Long), b: (Measure, Double, Long)): (Measure, Double, Long) =
    ( if ("__unknown__".equals(a._1.getLocation)) b._1 else a._1 , a._2 + b._2, a._3 + b._3)

  private def emptyMeasure() =
    Measure.newBuilder()
      .setLocation("__unknown__")
      .setTimeBlockId(0)
      .setMeasureType(MeasureType.TEMPERATURE)
      .setBucket(0)
      .setTimestamp(Instant.EPOCH)
      .setValue(0)
      .build()
}
