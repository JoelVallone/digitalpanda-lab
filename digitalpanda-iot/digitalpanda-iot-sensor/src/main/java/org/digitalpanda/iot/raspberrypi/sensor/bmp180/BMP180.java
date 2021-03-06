package org.digitalpanda.iot.raspberrypi.sensor.bmp180;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import org.digitalpanda.common.data.backend.SensorMeasureType;
import org.digitalpanda.common.data.backend.SensorMeasures;
import org.digitalpanda.iot.raspberrypi.sensor.Sensor;
import org.digitalpanda.iot.raspberrypi.sensor.SensorData;
import org.digitalpanda.iot.raspberrypi.sensor.SensorModel;
import org.digitalpanda.iot.raspberrypi.sensor.utils.EndianReaders;

import java.io.IOException;
import java.util.List;

/**
 * Temperature and pressure senor from Bosh
 * Initial source of the code: https://github.com/OlivierLD/raspberry-pi4j-samples/blob/6c8d1efd5ef4fc6e759db1e33a90eb65a0c4626f/I2C.SPI/src/i2c/sensor/BMP180.java
 */
public class BMP180 implements Sensor{
    private final static EndianReaders.Endianness BMP180_ENDIANNESS = EndianReaders.Endianness.BIG_ENDIAN;

    public final static int BMP180_I2C_DEVICE_ADDRESS = 0x77;
    // Operating Modes
    public final static int BMP180_OP_MODE_ULTRALOWPOWER = 0;
    public final static int BMP180_OP_MODE_STANDARD = 1;
    public final static int BMP180_OP_MODE_HIGHRES = 2;
    public final static int BMP180_OP_MODE_ULTRAHIGHRES = 3;

    // BMP180 Registers
    public final static int BMP180_CAL_AC1           = 0xAA;  // R   Calibration data (16 bits)
    public final static int BMP180_CAL_AC2           = 0xAC;  // R   Calibration data (16 bits)
    public final static int BMP180_CAL_AC3           = 0xAE;  // R   Calibration data (16 bits)
    public final static int BMP180_CAL_AC4           = 0xB0;  // R   Calibration data (16 bits)
    public final static int BMP180_CAL_AC5           = 0xB2;  // R   Calibration data (16 bits)
    public final static int BMP180_CAL_AC6           = 0xB4;  // R   Calibration data (16 bits)
    public final static int BMP180_CAL_B1            = 0xB6;  // R   Calibration data (16 bits)
    public final static int BMP180_CAL_B2            = 0xB8;  // R   Calibration data (16 bits)
    public final static int BMP180_CAL_MB            = 0xBA;  // R   Calibration data (16 bits)
    public final static int BMP180_CAL_MC            = 0xBC;  // R   Calibration data (16 bits)
    public final static int BMP180_CAL_MD            = 0xBE;  // R   Calibration data (16 bits)

    public final static int BMP180_CONTROL           = 0xF4;
    public final static int BMP180_TEMPDATA          = 0xF6;
    public final static int BMP180_PRESSUREDATA      = 0xF6;
    public final static int BMP180_READTEMPCMD       = 0x2E;
    public final static int BMP180_READPRESSURECMD   = 0x34;

    private int cal_AC1 = 0;
    private int cal_AC2 = 0;
    private int cal_AC3 = 0;
    private int cal_AC4 = 0;
    private int cal_AC5 = 0;
    private int cal_AC6 = 0;
    private int cal_B1  = 0;
    private int cal_B2  = 0;
    private int cal_MB  = 0;
    private int cal_MC  = 0;
    private int cal_MD  = 0;

    private static boolean verbose = "true".equals(System.getProperty("bmp180.verbose", "false"));

    private I2CBus bus;
    private I2CDevice bmp180;
    private int mode = BMP180_OP_MODE_STANDARD;
    private SensorData bmp180Data;
    private boolean initialized;

    public BMP180(){
        this.initialized = false;
    }

    private int readU16(int register) throws Exception
    {
        return EndianReaders.readU16(this.bmp180, BMP180_I2C_DEVICE_ADDRESS, register, BMP180_ENDIANNESS, verbose);
    }

    private int readS16(int register) throws Exception
    {
        return EndianReaders.readS16(this.bmp180, BMP180_I2C_DEVICE_ADDRESS, register, BMP180_ENDIANNESS, verbose);
    }

    public void readCalibrationData() throws Exception
    {
        // Reads the calibration data from the IC
        cal_AC1 = readS16(BMP180_CAL_AC1);   // INT16
        cal_AC2 = readS16(BMP180_CAL_AC2);   // INT16
        cal_AC3 = readS16(BMP180_CAL_AC3);   // INT16
        cal_AC4 = readU16(BMP180_CAL_AC4);   // UINT16
        cal_AC5 = readU16(BMP180_CAL_AC5);   // UINT16
        cal_AC6 = readU16(BMP180_CAL_AC6);   // UINT16
        cal_B1 =  readS16(BMP180_CAL_B1);    // INT16
        cal_B2 =  readS16(BMP180_CAL_B2);    // INT16
        cal_MB =  readS16(BMP180_CAL_MB);    // INT16
        cal_MC =  readS16(BMP180_CAL_MC);    // INT16
        cal_MD =  readS16(BMP180_CAL_MD);    // INT16
        if (verbose)
            showCalibrationData();
    }

    private void showCalibrationData()
    {
        // Displays the calibration values for debugging purposes
        System.out.println("DBG: AC1 = " + cal_AC1);
        System.out.println("DBG: AC2 = " + cal_AC2);
        System.out.println("DBG: AC3 = " + cal_AC3);
        System.out.println("DBG: AC4 = " + cal_AC4);
        System.out.println("DBG: AC5 = " + cal_AC5);
        System.out.println("DBG: AC6 = " + cal_AC6);
        System.out.println("DBG: B1  = " + cal_B1);
        System.out.println("DBG: B2  = " + cal_B2);
        System.out.println("DBG: MB  = " + cal_MB);
        System.out.println("DBG: MC  = " + cal_MC);
        System.out.println("DBG: MD  = " + cal_MD);
    }

    public int readRawTemp() throws Exception
    {
        // Reads the raw (uncompensated) temperature from the sensor
        bmp180.write(BMP180_CONTROL, (byte)BMP180_READTEMPCMD);
        waitfor(5);  // Wait 5ms
        int raw = readU16(BMP180_TEMPDATA);
        if (verbose)
            System.out.println("DBG: Raw Temp: " + (raw & 0xFFFF) + ", " + raw);
        return raw;
    }

    public int readRawPressure() throws Exception
    {
        // Reads the raw (uncompensated) pressure level from the sensor
        bmp180.write(BMP180_CONTROL, (byte)(BMP180_READPRESSURECMD + (this.mode << 6)));
        if (this.mode == BMP180_OP_MODE_ULTRALOWPOWER)
            waitfor(5);
        else if (this.mode == BMP180_OP_MODE_HIGHRES)
            waitfor(14);
        else if (this.mode == BMP180_OP_MODE_ULTRAHIGHRES)
            waitfor(26);
        else
            waitfor(8);
        int msb  = bmp180.read(BMP180_PRESSUREDATA);
        int lsb  = bmp180.read(BMP180_PRESSUREDATA + 1);
        int xlsb = bmp180.read(BMP180_PRESSUREDATA + 2);
        int raw = ((msb << 16) + (lsb << 8) + xlsb) >> (8 - this.mode);
        if (verbose)
            System.out.println("DBG: Raw Pressure: " + (raw & 0xFFFF) + ", " + raw);
        return raw;
    }

    public float readTemperature() throws Exception
    {
        // Gets the compensated temperature in degrees celcius
        int UT = 0;
        int X1 = 0;
        int X2 = 0;
        int B5 = 0;
        float temp = 0.0f;

        // Read raw temp before aligning it with the calibration values
        UT = this.readRawTemp();
        X1 = ((UT - this.cal_AC6) * this.cal_AC5) >> 15;
        X2 = (this.cal_MC << 11) / (X1 + this.cal_MD);
        B5 = X1 + X2;
        temp = ((B5 + 8) >> 4) / 10.0f;
        if (verbose)
            System.out.println("DBG: Calibrated temperature = " + temp + " C");
        return temp;
    }

    public float readPressure() throws Exception
    {
        // Gets the compensated pressure in pascal
        int UT = 0;
        int UP = 0;
        int B3 = 0;
        int B5 = 0;
        int B6 = 0;
        int X1 = 0;
        int X2 = 0;
        int X3 = 0;
        int p =  0;
        int B4 = 0;
        int B7 = 0;

        UT = this.readRawTemp();
        UP = this.readRawPressure();

        // You can use the datasheet values to test the conversion results
        // boolean dsValues = true;
        boolean dsValues = false;

        if (dsValues)
        {
            UT = 27898;
            UP = 23843;
            this.cal_AC6 = 23153;
            this.cal_AC5 = 32757;
            this.cal_MB = -32768;
            this.cal_MC = -8711;
            this.cal_MD = 2868;
            this.cal_B1 = 6190;
            this.cal_B2 = 4;
            this.cal_AC3 = -14383;
            this.cal_AC2 = -72;
            this.cal_AC1 = 408;
            this.cal_AC4 = 32741;
            this.mode = BMP180_OP_MODE_ULTRALOWPOWER;
            if (verbose)
                this.showCalibrationData();
        }
        // True Temperature Calculations
        X1 = (int)((UT - this.cal_AC6) * this.cal_AC5) >> 15;
        X2 = (this.cal_MC << 11) / (X1 + this.cal_MD);
        B5 = X1 + X2;
        if (verbose)
        {
            System.out.println("DBG: X1 = " + X1);
            System.out.println("DBG: X2 = " + X2);
            System.out.println("DBG: B5 = " + B5);
            System.out.println("DBG: True Temperature = " + (((B5 + 8) >> 4) / 10.0)  + " C");
        }
        // Pressure Calculations
        B6 = B5 - 4000;
        X1 = (this.cal_B2 * (B6 * B6) >> 12) >> 11;
        X2 = (this.cal_AC2 * B6) >> 11;
        X3 = X1 + X2;
        B3 = (((this.cal_AC1 * 4 + X3) << this.mode) + 2) / 4;
        if (verbose)
        {
            System.out.println("DBG: B6 = " + B6);
            System.out.println("DBG: X1 = " + X1);
            System.out.println("DBG: X2 = " + X2);
            System.out.println("DBG: X3 = " + X3);
            System.out.println("DBG: B3 = " + B3);
        }
        X1 = (this.cal_AC3 * B6) >> 13;
        X2 = (this.cal_B1 * ((B6 * B6) >> 12)) >> 16;
        X3 = ((X1 + X2) + 2) >> 2;
        B4 = (this.cal_AC4 * (X3 + 32768)) >> 15;
        B7 = (UP - B3) * (50000 >> this.mode);
        if (verbose)
        {
            System.out.println("DBG: X1 = " + X1);
            System.out.println("DBG: X2 = " + X2);
            System.out.println("DBG: X3 = " + X3);
            System.out.println("DBG: B4 = " + B4);
            System.out.println("DBG: B7 = " + B7);
        }
        if (B7 < 0x80000000)
            p = (B7 * 2) / B4;
        else
            p = (B7 / B4) * 2;

        if (verbose)
            System.out.println("DBG: X1 = " + X1);

        X1 = (p >> 8) * (p >> 8);
        X1 = (X1 * 3038) >> 16;
        X2 = (-7357 * p) >> 16;
        if (verbose)
        {
            System.out.println("DBG: p  = " + p);
            System.out.println("DBG: X1 = " + X1);
            System.out.println("DBG: X2 = " + X2);
        }
        p = p + ((X1 + X2 + 3791) >> 4);
        if (verbose)
            System.out.println("DBG: Pressure = " + p + " Pa");

        return p;
    }

    protected static void waitfor(long howMuch)
    {
        try { Thread.sleep(howMuch); } catch (InterruptedException ie) { ie.printStackTrace(); }
    }

    @Override
    public boolean initialize() {
        boolean initialized = true;
        try {
            // Get i2c bus
            bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends on the RasPI version
            if (verbose)
                System.out.println("Connected to bus. OK.");

            // Get device itself
            bmp180 = bus.getDevice(BMP180_I2C_DEVICE_ADDRESS);
            if (verbose)
                System.out.println("Connected to device. OK.");

            try { this.readCalibrationData(); }
            catch (Exception ex)
            { ex.printStackTrace(); }
        } catch (IOException | I2CFactory.UnsupportedBusNumberException e) {
            e.printStackTrace();
            initialized =  false;
        }
        return this.initialized = initialized;
    }

    @Override
    public void calibrate(List<SensorMeasures> allAvailableMeasures) {
        // nop
    }

    @Override
    public SensorData fetchAndComputeValues() throws IOException {
        if(!initialized){
            if(!initialize()){
                return null;
            }
        }
        double press = 0;
        double temp  = 0;

        try { press = this.readPressure(); }
        catch (Exception ex)
        {
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }
        try { temp = this.readTemperature(); }
        catch (Exception ex)
        {
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }

        this.bmp180Data = (new SensorData(SensorModel.BMP180))
                .setSensorData(SensorMeasureType.TEMPERATURE, temp)
                .setSensorData(SensorMeasureType.PRESSURE, press / 100);
        return this.bmp180Data;
    }

    @Override
    public SensorData getLastRecord() {
        return bmp180Data;
    }
}
