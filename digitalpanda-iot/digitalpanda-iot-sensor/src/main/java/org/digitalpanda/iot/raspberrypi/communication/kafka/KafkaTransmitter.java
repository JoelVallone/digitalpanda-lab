package org.digitalpanda.iot.raspberrypi.communication.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.digitalpanda.avro.MeasureType;
import org.digitalpanda.avro.RawMeasure;
import org.digitalpanda.backend.data.SensorMeasureMetaData;
import org.digitalpanda.backend.data.SensorMeasures;
import org.digitalpanda.iot.raspberrypi.communication.MeasureTransmitter;

import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

public class KafkaTransmitter implements MeasureTransmitter {

    private final KafkaProducer<String, RawMeasure> kafkaProducer;
    private String targetTopic;

    public KafkaTransmitter(String targetTopic, String clientId, String bootstrapServers){

        Properties config = new Properties();
        config.put("client.id",clientId);
        config.put("bootstrap.servers", bootstrapServers);
        config.put("acks", "all");
        this.kafkaProducer = new KafkaProducer<>(config);
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
                .forEach(kafkaProducer::send);
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
