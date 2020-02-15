package org.digitalpanda.flink.sensor.digestion.operators

import org.apache.flink.api.common.functions.AggregateFunction

case class AverageAggregate[IN](valueExtractor: Function[IN, Double] ) extends AggregateFunction[IN, (Double, Long), Double] {

  override def createAccumulator(): (Double, Long) = (0.0, 0L)

  override def add(measure: IN, acc:  (Double, Long)): (Double, Long) =
    (acc._1 + valueExtractor(measure), acc._2 + 1L)

  override def getResult(acc: (Double, Long)): Double = acc._1 / acc._2

  override def merge(a: (Double, Long), b: (Double, Long)): (Double, Long) =
    (a._1 + b._1, a._2 + b._2)
}
