package org.digitalpanda.backend.application.northbound.ressource.measure;

import java.util.Objects;

public class SensorMeasureDTO {

    private long timestamp;
    private double value;

    public SensorMeasureDTO(long timestamp, double value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public long getTimestamp() { return timestamp; }
    public double getValue() { return value; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setValue(double value) { this.value = value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SensorMeasureDTO that = (SensorMeasureDTO) o;
        return timestamp == that.timestamp &&
                Double.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, value);
    }

    @Override
    public String toString(){
        return "timestamp=" + timestamp + ", value=" + value;
    }
}
