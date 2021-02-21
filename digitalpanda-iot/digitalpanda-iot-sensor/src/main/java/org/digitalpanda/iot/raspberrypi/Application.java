package org.digitalpanda.iot.raspberrypi;


import org.digitalpanda.common.data.backend.SensorMeasures;
import org.digitalpanda.iot.raspberrypi.communication.MeasureTransmitter;
import org.digitalpanda.iot.raspberrypi.communication.TransmitterFactory;
import org.digitalpanda.iot.raspberrypi.sensor.Sensor;
import org.digitalpanda.iot.raspberrypi.sensor.SensorData;
import org.digitalpanda.iot.raspberrypi.sensor.SensorModel;
import org.digitalpanda.iot.raspberrypi.sensor.utils.SensorDataMapper;
import org.digitalpanda.iot.raspberrypi.sensor.utils.SensorFactory;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.digitalpanda.iot.raspberrypi.Configuration.ConfigurationKey.SENSOR_LOCATION;
import static org.digitalpanda.iot.raspberrypi.Configuration.ConfigurationKey.SENSOR_MODELS;

public class Application {

    private Configuration conf;
    private List<MeasureTransmitter> transmitters;
    private List<Sensor> sensors;


    public static void main(String...args) {
        (new Application()).start();
    }

    private Application() {}

    private void start(){
        if (!this.init()) {
            System.exit(1);
        }
        while (true) {
            try {
                long startTime = System.currentTimeMillis();
                List<SensorMeasures> measures = fetchAndDisplayMeasuresFromSensors();
                transmitters.forEach(trm -> trm.sendMeasures(measures));
                long sleepTime = 1000L - (System.currentTimeMillis() - startTime);
                Thread.sleep(sleepTime > 0L ? sleepTime : 1L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean init(){
        this.conf = Configuration.getInstance();
        System.out.println("Configuration: \n " + conf);

        this.transmitters = TransmitterFactory.loadMeasureTransmitters(conf);
        if(transmitters.size() == 0){
            System.err.println("No transmitters available");
        } else {
            System.out.println("Transmitter instance(s): \n" +
                    transmitters.stream().map(t -> " - " + t.getClass().getCanonicalName() + "\n").collect(joining()));
        }

        this.sensors = loadSensors(conf);
        if (sensors == null || sensors.size() < 1) {
            System.err.println("No valid sensor selected");
        }

        return transmitters.size() > 0 && sensors.size() == initSensors(sensors);
    }

    private  static List<Sensor> loadSensors(Configuration conf) {
        String sensorModels = Optional.ofNullable(conf.getString(SENSOR_MODELS)).orElse("");
        return Arrays.stream(sensorModels.split(","))
                .map(s -> loadSensor(s.trim().toUpperCase(), conf))
                .collect(toList());
    }

    private static Sensor loadSensor(String sensorModelName, Configuration conf) {
        SensorModel sensorModel = SensorModel.valueOf(sensorModelName);
        // System.out.println(sensorModelName + ">," + (new SensorData(sensorModel)).csvHeader());
        return SensorFactory.buildSensor(sensorModel, conf);
    }

    private int initSensors(List<Sensor> sensors) {
        int initSuccessCount = 0;
        for(Sensor sensor: sensors) {
            String sensorName = sensor.getClass().getCanonicalName();
            if (sensor.initialize()) {
                System.out.println(sensorName + " initialized");
                initSuccessCount += 1;
            } else {
                System.err.println(sensorName + " initialization failure");
            }
        }
        return initSuccessCount;
    }

    private List<SensorMeasures> fetchAndDisplayMeasuresFromSensors() {
        List<SensorMeasures> allLatestMeasures =  this.sensors.stream()
                .flatMap(this::fetchAndDisplayMeasuresFromSensor)
                .collect(toList());
        this.sensors.forEach(s -> s.calibrate(allLatestMeasures));
        return allLatestMeasures;
    }

    private Stream<SensorMeasures> fetchAndDisplayMeasuresFromSensor(Sensor sensor)  {
        try {
            SensorData sensorData = sensor.fetchAndComputeValues();
            // System.out.println(">," + sensorData.csvData());
            return SensorDataMapper.create(sensorData, conf.getString(SENSOR_LOCATION)).stream();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return Stream.empty();
        }
    }
}
