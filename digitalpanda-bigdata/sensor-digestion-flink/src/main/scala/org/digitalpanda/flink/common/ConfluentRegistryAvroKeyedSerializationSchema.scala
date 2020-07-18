package org.digitalpanda.flink.common

import java.lang

import org.apache.avro.specific.SpecificRecord
import org.apache.flink.formats.avro.registry.confluent.ConfluentRegistryAvroSerializationSchema
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema
import org.apache.kafka.clients.producer.ProducerRecord

//Ref:  https://stackoverflow.com/questions/59982631/flink-avro-serialization-shows-not-serializable-error-when-working-with-generi
class ConfluentRegistryAvroKeyedSerializationSchema[V<: SpecificRecord](tClass: Class[V], topic: String , schemaRegistryUrl: String) extends KafkaSerializationSchema[(String, V)] {

  private val valueDeserializer =
    ConfluentRegistryAvroSerializationSchema.forSpecific(tClass,s"$topic-value", schemaRegistryUrl)

  private def serializeKey(tuple: (String, V)): Array[Byte] = tuple._1.getBytes
  private def serializeValue(tuple: (String, V)): Array[Byte]  = valueDeserializer.serialize(tuple._2)

  override def serialize(tuple: (String, V), aLong: lang.Long): ProducerRecord[Array[Byte], Array[Byte]] =
    new ProducerRecord(topic, serializeKey(tuple), serializeValue(tuple))
}
