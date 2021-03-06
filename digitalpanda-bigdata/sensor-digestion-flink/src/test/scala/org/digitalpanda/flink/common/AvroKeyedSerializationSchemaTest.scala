package org.digitalpanda.flink.common

import org.apache.flink.formats.avro.AvroDeserializationSchema
import org.digitalpanda.common.data.avro.Measure
import org.digitalpanda.common.data.avro.MeasureType.TEMPERATURE
import org.digitalpanda.common.data.history.HistoricalDataStorageSizing
import org.digitalpanda.flink.test.TestHelper.measure
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AvroKeyedSerializationSchemaTest extends AnyFlatSpec with Matchers {

  "AvroKeyedSerializationSchema" should "serialize an Avro SpecificRecord" in {
    // Given
    val inputSpecificRecord = measure(HistoricalDataStorageSizing.SECOND_PRECISION_RAW)("server-room",  TEMPERATURE, "2019-06-30T22:09:59Z", 26.0)
    val uut = new AvroKeyedSerializationSchema(classOf[Measure])

    // When
    val actualBytes = uut.serializeValue(("key", inputSpecificRecord))

    // Then
    val actual = AvroDeserializationSchema.forSpecific(classOf[Measure]).deserialize(actualBytes)
    actual should equal (inputSpecificRecord)
  }
}
