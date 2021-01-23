package org.digitalpanda.common.data.backend;

public enum SensorMeasureType {

    TEMPERATURE("Degree Celcius", "°C"),
    HUMIDITY("Relative humidity", "%"),
    PRESSURE("Hecto-Pascal","hPa"),
    eCO2("Equivalent CO2 particle Part Per Million","ppm"),
    TVOC("Total Volatile Organic Compounds Part Per Million","ppm");

    private final String unit;
    private final String description;

    SensorMeasureType(String description, String unit) {
        this.description = description;
        this.unit = unit;
    }

    @Override
    public String toString(){
        return description + "[" + unit + "]";
    }

    public String getUnit() {
        return unit;
    }

    public String getDescription() {
        return description;
    }
}
