package org.digitalpanda.iot.raspberrypi.sensor.utils;

import org.digitalpanda.iot.raspberrypi.Configuration;
import org.digitalpanda.iot.raspberrypi.sensor.Sensor;
import org.digitalpanda.iot.raspberrypi.sensor.SensorModel;
import org.digitalpanda.iot.raspberrypi.sensor.bme280.BME280;
import org.digitalpanda.iot.raspberrypi.sensor.bmp180.BMP180;
import org.digitalpanda.iot.raspberrypi.sensor.sgp30.SGP30;

import java.util.Optional;

public class SensorFactory {

    public static Sensor buildSensor(SensorModel sensorModel, Configuration conf){
        switch (sensorModel) {
            case BME280:
                return new BME280();
            case BMP180:
                return new BMP180();
            case SGP30:
                return new SGP30(conf);
            default:
                System.out.println("WARNING: unknown sensor: " + sensorModel);
                return null;
        }
    }
}
