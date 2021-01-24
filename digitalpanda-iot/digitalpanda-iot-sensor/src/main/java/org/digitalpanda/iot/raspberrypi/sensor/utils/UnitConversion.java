package org.digitalpanda.iot.raspberrypi.sensor.utils;

import java.util.Optional;

public class UnitConversion {

    /**
     * Converts relative humidity to absolute humidity in grams of water per cubic meters
     *
     * Reference: "Humidity Compensation" section from Sensirion's GP30 data-sheet
     *
     * @param tempCelsius Temperature in degree celsius measured in pair with the humidity. Allowed value: [-40; 80]
     * @param relativeHumidity Relative humidity from 0 to 100 to convert. Allowed value: [0,100]
     * @return Absolute humidity in g/m^3 or empty if the input is out of the conversion tolerance boundaries
     */
    public static Optional<Double> tryComputeHumidityGramsPerCubicMeter(double tempCelsius, double relativeHumidity) {

        if (tempCelsius < -40.0 || tempCelsius > 80.0 || relativeHumidity < 0 || relativeHumidity > 100) {
            return Optional.empty();
        }

        double humidityGramsPerCubicMeter =
                216.7 * (
                        ((relativeHumidity/100.0) * 6.112 * Math.exp((17.62*tempCelsius)/(243.12+tempCelsius)))
                                /
                                (273.15 + tempCelsius)
                );
        return Optional.of(humidityGramsPerCubicMeter);
    }
}
