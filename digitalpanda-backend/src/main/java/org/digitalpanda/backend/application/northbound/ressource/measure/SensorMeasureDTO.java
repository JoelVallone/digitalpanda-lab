package org.digitalpanda.backend.application.northbound.ressource.measure;

import org.digitalpanda.common.data.backend.SensorMeasureType;

import java.util.Objects;

public class SensorMeasureDTO {

    private String location;
    private SensorMeasureType type;
    private long timestamp;
    private double value;

    public SensorMeasureDTO(long timestamp, double value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public SensorMeasureDTO(String location, SensorMeasureType type, long timestamp, double value) {
        this.location = location;
        this.type = type;
        this.timestamp = timestamp;
        this.value = value;
    }

    public String getLocation() {
        return location;
    }

    public SensorMeasureType getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SensorMeasureDTO that = (SensorMeasureDTO) o;
        return timestamp == that.timestamp &&
                Double.compare(that.value, value) == 0 &&
                Objects.equals(location, that.location) &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, type, timestamp, value);
    }

    @Override
    public String toString() {
        return "SensorMeasureDTO{" +
                "location='" + location + '\'' +
                ", type='" + type + '\'' +
                ", timestamp=" + timestamp +
                ", value=" + value +
                '}';
    }
}
