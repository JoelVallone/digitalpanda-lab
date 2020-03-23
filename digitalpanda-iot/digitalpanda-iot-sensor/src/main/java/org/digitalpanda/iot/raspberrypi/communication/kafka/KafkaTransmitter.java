package org.digitalpanda.iot.raspberrypi.communication.kafka;

import org.apache.kafka.clients.producer.*;
import org.digitalpanda.common.data.avro.MeasureType;
import org.digitalpanda.common.data.avro.RawMeasure;
import org.digitalpanda.common.data.backend.SensorMeasureMetaData;
import org.digitalpanda.common.data.backend.SensorMeasures;
import org.digitalpanda.iot.raspberrypi.communication.MeasureTransmitter;

import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.AUTO_REGISTER_SCHEMAS;
import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.*;

public class KafkaTransmitter implements MeasureTransmitter {

    private final KafkaProducer<String, RawMeasure> kafkaProducer;
    private String targetTopic;

    public KafkaTransmitter(String targetTopic, String clientId, String bootstrapServers, String schemaRegistryUrl){

        Properties props = new Properties();
        props.put(CLIENT_ID_CONFIG,                 clientId);
        props.put(BOOTSTRAP_SERVERS_CONFIG,         bootstrapServers);
        props.put(ACKS_CONFIG,                      "all");
        props.put(SCHEMA_REGISTRY_URL_CONFIG,       schemaRegistryUrl);
        props.put(AUTO_REGISTER_SCHEMAS,            false);
        props.put(VALUE_SERIALIZER_CLASS_CONFIG,    io.confluent.kafka.serializers.KafkaAvroSerializer.class);
        props.put(KEY_SERIALIZER_CLASS_CONFIG,      org.apache.kafka.common.serialization.StringSerializer.class);

        this.kafkaProducer = new KafkaProducer<>(props);
        this.targetTopic = targetTopic;
    }

    @Override
    public void sendMeasures(List<SensorMeasures> sensorData) {
        sensorData.stream()
                .flatMap(this::toRawMeasures)
                .map(rawMeasure -> new ProducerRecord<>(
                        targetTopic,
                        rawMeasure.getLocation() + "-" + rawMeasure.getMeasureType(),
                        rawMeasure))
                .forEach(this::sendRecord);
    }

    private void sendRecord(ProducerRecord<String, RawMeasure> record) {
        kafkaProducer.send(
                record,
                (RecordMetadata metadata, Exception e) -> {
                    if ( e != null) {
                        System.err.println("Send failed for record: " + record);
                        e.printStackTrace();
                    }});
    }

    private Stream<RawMeasure> toRawMeasures(SensorMeasures sensorMeasures) {
        SensorMeasureMetaData measureMetaData = sensorMeasures.getSensorMeasureMetaData();
        return sensorMeasures.getMeasures().stream()
                .map(sensorMeasure ->
                    RawMeasure.newBuilder()
                        .setLocation(measureMetaData.getLocation())
                        .setMeasureType(MeasureType.valueOf(measureMetaData.getType().name()))
                        .setTimestamp(Instant.ofEpochMilli(sensorMeasure.getTimestamp()))
                        .setValue(sensorMeasure.getValue())
                    .build());
    }
}
