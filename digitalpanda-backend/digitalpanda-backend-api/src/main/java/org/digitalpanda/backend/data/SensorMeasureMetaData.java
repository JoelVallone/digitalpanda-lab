package org.digitalpanda.backend.data;

public class SensorMeasureMetaData {

    private String location;
    private SensorMeasureType type;

    public SensorMeasureMetaData() {this (null,null);}

    public SensorMeasureMetaData(String location, SensorMeasureType type) {
        this.location = location;
        this.type = type;
    }

    public String getLocation() {return location; }
    public SensorMeasureType getType() { return type; }

    public void setLocation(String location) { this.location = location; }
    public void setType(SensorMeasureType type) { this.type = type; }

    @Override
    public String toString(){
        return "location=" + location + ", type=" + (type != null ? type.name() : null);
    }

    @Override
    public int hashCode(){
        return (location + type).hashCode();
    }

    @Override
    public boolean equals(Object o){
        if(! (o instanceof SensorMeasureMetaData)) return false;
        SensorMeasureMetaData object = (SensorMeasureMetaData) o;
        return  ((object.location == null && this.location == null) || (object.location != null && object.location.equals(this.location)))
                &&
                ((object.type == null && this.type == null) || (object.type != null && object.type.equals(this.type)));
    }
}
