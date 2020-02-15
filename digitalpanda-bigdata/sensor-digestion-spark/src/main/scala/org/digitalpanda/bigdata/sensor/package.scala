package org.digitalpanda.bigdata

package object sensor {
  type Location = String
  case class AnonymousMeasure(timestamp: Long, value: Double)
  case class Aggregate(location: String,
                       time_block_id: Long,
                       measure_type: String,
                       bucket: Int,
                       timestamp: Long,
                       value: Double)
}
