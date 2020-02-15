package org.digitalpanda.iot.raspberrypi.communication;

import org.digitalpanda.iot.raspberrypi.Configuration;
import org.digitalpanda.iot.raspberrypi.communication.http.HttpTransmitter;
import org.digitalpanda.iot.raspberrypi.communication.kafka.KafkaTransmitter;

import java.util.LinkedList;
import java.util.List;

import static org.digitalpanda.iot.raspberrypi.Configuration.ConfigurationKey.*;

public class TransmitterFactory {

    public static List<MeasureTransmitter> loadMeasureTransmitters(Configuration conf) {
        List<MeasureTransmitter> transmitters = new LinkedList<>();
        if (conf.getBoolean(REST_BACKEND_ENABLED)) {
            try {
                transmitters.add(new HttpTransmitter(conf.getString(REST_BACKEND_URL)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (conf.getBoolean(KAFKA_PRODUCER_ENABLED)){
            transmitters.add(
                    new KafkaTransmitter(
                            conf.getString(KAFKA_PRODUCER_TOPIC),
                            conf.getString(KAFKA_PRODUCER_ID),
                            conf.getString(KAFKA_PRODUCER_SERVERS)
                    ));
        }
        return transmitters;
    }
}
