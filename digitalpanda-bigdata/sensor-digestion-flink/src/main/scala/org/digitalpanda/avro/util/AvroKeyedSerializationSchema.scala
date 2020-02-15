package org.digitalpanda.avro.util

import java.io.{ByteArrayOutputStream, IOException}

import org.apache.avro.specific.SpecificRecord
import org.apache.beam.sdk.coders.{AvroCoder, Coder}
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema

class AvroKeyedSerializationSchema[V<: SpecificRecord](tClass: Class[V]) extends KeyedSerializationSchema[Pair[String, V]] {

  val coder: AvroCoder[V] = AvroCoder.of(tClass)
  val out: ByteArrayOutputStream = new ByteArrayOutputStream

  override def serializeKey(element: (String, V)): Array[Byte] = element._1.getBytes

  override def serializeValue(element: (String, V)): Array[Byte]  = {
    try {
      out.reset()
      coder.encode(element._2, out, Coder.Context.NESTED)
    } catch {
      case e: IOException => throw new RuntimeException("Avro encoding failed.", e)
    }
    out.toByteArray
  }

  override def getTargetTopic(element: (String, V)): String =
    null // we are never overriding the topic

}
