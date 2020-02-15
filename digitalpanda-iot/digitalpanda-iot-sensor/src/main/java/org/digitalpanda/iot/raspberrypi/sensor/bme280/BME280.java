package org.digitalpanda.iot.raspberrypi.sensor.bme280;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import org.digitalpanda.backend.data.SensorMeasureType;
import org.digitalpanda.iot.raspberrypi.sensor.Sensor;
import org.digitalpanda.iot.raspberrypi.sensor.SensorData;
import org.digitalpanda.iot.raspberrypi.sensor.SensorModel;

import java.io.IOException;

/**
 * Humidity, Temperature and pressure senor from Bosh
 * Initial source of the code: https://github.com/ControlEverythingCommunity/BME280/blob/master/Java/BME280.java
 */
public class BME280 implements Sensor {

    private static final int BME280_I2C_DEVICE_ADDR = 0x77;

    //Trimming parameter registers
    private static final byte BME280_REGISTER_DIG_T1 = (byte) 0x88;
    private static final byte BME280_REGISTER_DIG_P1 = (byte) 0x8E;
    private static final byte BME280_REGISTER_DIG_H1 = (byte) 0xA1;
    private static final byte BME280_REGISTER_DIG_H2 = (byte) 0xE1;

    //data registers
    private static final byte BME280_REGISTER_CHIP_ID = (byte) 0xD0;
    private static final byte BME280_REGISTER_CONTROL_HUM = (byte) 0xF2;
    private static final byte BME280_REGISTER_CONTROL = (byte) 0xF4;
    private static final byte BME280_REGISTER_CONFIG = (byte) 0xF5;
    private static final byte BME280_REGISTER_PRESSURE_DATA = (byte) 0xF7;
    private static final byte BME280_REGISTER_TEMP_DATA = (byte) 0xFA;
    private static final byte BME280_REGISTER_HUMIDITY_DATA = (byte) 0xFD;

    private SensorData bme280Data;
    private I2CBus i2c;
    private I2CDevice device;
    private int[] dig_T, dig_P, dig_H;
    private boolean initialized;
    private int chipId;

    public BME280(){
        this.initialized = false;
    }

    public SensorData fetchAndComputeValues() throws IOException{
        if(!initialized){
            if(!initialize()){
                return null;
            }
        }
        // Select control humidity register
        // Humidity over sampling rate = 1
        device.write(BME280_REGISTER_CONTROL_HUM , (byte)0x01);
        // Select control measurement register
        // Normal mode, temp and pressure over sampling rate = 1
        device.write(BME280_REGISTER_CONTROL , (byte)0x27);
        // Select config register
        // Stand_by time = 1000 ms
        device.write(BME280_REGISTER_CONFIG , (byte)0xA0);

        // Read 8 bytes of data from address 0xF7(247)
        //read pressure msb1, pressure msb, pressure lsb,
        byte[] data = new byte[8];
        device.read(BME280_REGISTER_PRESSURE_DATA, data, 0, 3);
        // Convert pressure data to 19-bits
        long adc_p = (((long)(data[0] & 0xFF) * (0x1 << 16)) + ((long)(data[1] & 0xFF) * (0x1 << 8)) + (long)(data[2] & 0xF0)) / (0x1 << 4);


        //read temp msb1, temp msb, temp lsb,
        device.read(BME280_REGISTER_TEMP_DATA, data, 0, 3);
        // Convert temperature data to 19-bits
        long adc_t = (((long)(data[0] & 0xFF) * (0x1 << 16)) + ((long)(data[1] & 0xFF) * (0x1 << 8)) + (long)(data[2] & 0xF0)) / (0x1 << 4);

        //read humidity lsb, humidity msb
        device.read(BME280_REGISTER_HUMIDITY_DATA, data, 0, 2);
        // Convert the humidity data to 16 bits
        long adc_h = ((long)(data[0] & 0xFF) * (0x1 << 8) + (long)(data[1] & 0xFF));

        // Temperature offset calculations
        double var1 = (((double)adc_t) / 16384.0 - ((double)dig_T[0]) / 1024.0) * ((double)dig_T[1]);
        double var2 = ((((double)adc_t) / 131072.0 - ((double)dig_T[0]) / 8192.0) *
                (((double)adc_t)/131072.0 - ((double)dig_T[0])/8192.0)) * ((double)dig_T[2]);
        double t_fine = (long)(var1 + var2);
        double temperatureInDegreeCelsius = (var1 + var2) / 5120.0;

        // Pressure offset calculations
        var1 = ((double)t_fine / 2.0) - 64000.0;
        var2 = var1 * var1 * ((double)dig_P[5]) / 32768.0;
        var2 = var2 + var1 * ((double)dig_P[4]) * 2.0;
        var2 = (var2 / 4.0) + (((double)dig_P[3]) * 65536.0);
        var1 = (((double) dig_P[2]) * var1 * var1 / 524288.0 + ((double) dig_P[1]) * var1) / 524288.0;
        var1 = (1.0 + var1 / 32768.0) * ((double)dig_P[0]);
        double p = 1048576.0 - (double)adc_p;
        p = (p - (var2 / 4096.0)) * 6250.0 / var1;
        var1 = ((double) dig_P[8]) * p * p / 2147483648.0;
        var2 = p * ((double) dig_P[7]) / 32768.0;
        double pressureInHpa = (p + (var1 + var2 + ((double)dig_P[6])) / 16.0) / 100;

        // Humidity offset calculations
        double var_H = (((double)t_fine) - 76800.0);
        var_H = (adc_h - (dig_H[3] * 64.0 + dig_H[4] / 16384.0 * var_H)) * (dig_H[1] / 65536.0 * (1.0 + dig_H[5] / 67108864.0 * var_H * (1.0 + dig_H[2] / 67108864.0 * var_H)));
        double humidity = var_H * (1.0 -  dig_H[0] * var_H / 524288.0);
        if(humidity > 100.0) { humidity = 100.0;
        }else if(humidity < 0.0) {  humidity = 0.0;  }
        double humidityInPercent = humidity;

        this.bme280Data = (new SensorData(SensorModel.BME280))
                .setSensorData(SensorMeasureType.TEMPERATURE, temperatureInDegreeCelsius)
                .setSensorData(SensorMeasureType.PRESSURE, pressureInHpa)
                .setSensorData(SensorMeasureType.HUMIDITY, humidityInPercent);
        return this.bme280Data;
    }

    public int getChipId(){
        return chipId;
    }

    public SensorData getLastRecord(){
        return this.bme280Data;
    }


    public boolean initialize(){
        boolean initialized = true;
        try {
            //connect to device
            i2c = I2CFactory.getInstance(I2CBus.BUS_1);
            device = i2c.getDevice(BME280_I2C_DEVICE_ADDR);
            chipId = device.read(BME280_REGISTER_CHIP_ID);

            //load calibration values
            // Read && convert the trimming temperature coefficients
            byte[] buffer = new byte[24];
            dig_T = new int [3];
            device.read(BME280_REGISTER_DIG_T1, buffer, 0, dig_T.length << 1);
            preProcessTrimmingDigits_TorP(dig_T, buffer);

            // Read && convert the trimming pressure coefficients
            dig_P = new int [9];
            device.read(BME280_REGISTER_DIG_P1, buffer, 0, dig_P.length << 1);
            preProcessTrimmingDigits_TorP(dig_P, buffer);

            // Read && convert the trimming humidity coefficients
            dig_H = new int[8];
            dig_H[0] = ((byte)device.read(BME280_REGISTER_DIG_H1) & 0xFF);
            device.read(BME280_REGISTER_DIG_H2, buffer, 0, dig_H.length -1);
            preProcessTrimmingDigits_H(dig_H, buffer);

        } catch (IOException | I2CFactory.UnsupportedBusNumberException e) {
            e.printStackTrace();
            initialized = false;
        }
        return this.initialized = initialized;
    }

    private void preProcessTrimmingDigits_TorP(int[] dig_TorP, byte[] buffer){
        //T1/P1 <-> dig_TorP[0] is unsigned 16 bit integer which must be stored in an 32 bit java integer
        dig_TorP[0] = (buffer[0] & 0xFF) + ((buffer[1] & 0xFF) << 8);
        //others are two's complement signed 16 bit integer which must be stored in an 32 bit java integer
        for(int i = 1; i < dig_TorP.length; i++){
            dig_TorP[i] = ((buffer[(i << 1) + 1] & 0xFF) << 8) + (buffer[i << 1] & 0xFF);
            //Toggle the sign if necessary
            if(dig_TorP[i] > ((0x1 << 15) - 1) ){ dig_TorP[i] -= (0x1 << 16); }
        }
    }

    private void preProcessTrimmingDigits_H(int [] dig_H, byte[]  buffer){
        //H1 <-> dig_H[0] is unsigned 8 bit integer which must be stored in an 32 bit java integer
        // => already read

        //H2 <-> dig_H[1] is signed 16 bit integer which must be stored in an 32 bit java integer
        dig_H[1] = ((buffer[1] & 0xFF) << 8) + (buffer[0] & 0xFF);
        //Toggle the sign if necessary
        if(dig_H[1] > ((0x1 << 15) - 1)){ dig_H[1] -= (0x1 << 16); }

        //H3 <-> dig_H[2] is unsigned 8 bit integer which must be stored in an 32 bit java integer
        dig_H[2] = buffer[2] & 0xFF ;

        //H4 <-> dig_H[3] is signed 16 bit integer (stored in a special way) which must be stored in an 32 bit java integer
        dig_H[3] = ((buffer[3] & 0xFF) * 16) + (buffer[4] & 0xF);
        if(dig_H[3] > ((0x1 << 15) - 1)){ dig_H[3] -= (0x1 << 16); }

        //H5 <-> dig_H[4] is signed 16 bit integer (stored in another special way) which must be stored in an 32 bit java integer
        dig_H[4] = ((buffer[4] & 0xFF) / 16) + ((buffer[5] & 0xFF) * 16);
        if(dig_H[4] > ((0x1 << 15) - 1)){ dig_H[4] -= (0x1 << 16); }

        //H6 <-> dig_H[5] is signed 8 bit integer which must be stored in an 32 bit java integer
        if(dig_H[5] > ((0x1 << 7) - 1)){ dig_H[5] -= (0x1 << 8); }
    }
}
