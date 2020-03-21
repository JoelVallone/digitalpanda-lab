package org.digitalpanda.flink.common


import java.io.{ByteArrayOutputStream, IOException}

import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.{SpecificData, SpecificDatumWriter, SpecificRecord}
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema

// TODO: Replace with ConfluentRegistryAvroSerializationSchema class https://issues.apache.org/jira/browse/FLINK-9679 with Flink 1.11
//@SerialVersionUID(1L)
class AvroKeyedSerializationSchema[V<: SpecificRecord](tClass: Class[V]) extends KeyedSerializationSchema[(String, V)] {

  @transient lazy private val datumWriter = new SpecificDatumWriter[V](SpecificData.get.getSchema(tClass));
  @transient lazy private val arrayOutputStream = new ByteArrayOutputStream
  @transient lazy private val encoder = EncoderFactory.get.directBinaryEncoder(arrayOutputStream, null)

  override def serializeKey(element: (String, V)): Array[Byte] = element._1.getBytes

  override def serializeValue(element: (String, V)): Array[Byte]  = {
    try {
      datumWriter.write(element._2, encoder);
      encoder.flush();
      val bytes: Array[Byte] = arrayOutputStream.toByteArray;
      arrayOutputStream.reset();
      bytes
    } catch {
      case e: IOException => throw new RuntimeException("Avro encoding failed.", e)
    }
  }

  override def getTargetTopic(element: (String, V)): String =
    null // we are never overriding the topic

}
