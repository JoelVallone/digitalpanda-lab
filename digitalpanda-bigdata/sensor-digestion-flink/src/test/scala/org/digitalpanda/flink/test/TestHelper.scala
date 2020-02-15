package org.digitalpanda.flink.test

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}

import org.digitalpanda.avro.{Measure, MeasureType}
import org.digitalpanda.backend.data.history.{HistoricalDataStorageHelper, HistoricalDataStorageSizing}

object TestHelper {

  def measure(blockUnit: HistoricalDataStorageSizing)(location: String, measureType: MeasureType, zuluTime: String, value: Double): Measure = {
    val time = ZonedDateTime.parse(zuluTime, DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault()))
    Measure.newBuilder()
      .setLocation(location)
      .setMeasureType(measureType)
      .setTimeBlockId(
        HistoricalDataStorageHelper
          .getHistoricalMeasureBlockId(
            time.toInstant.toEpochMilli,
            blockUnit))
      .setBucket(0)
      .setTimestamp(time.toInstant)
      .setValue(value)
      .build()
  }

}