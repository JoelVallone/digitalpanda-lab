package org.digitalpanda.iot.raspberrypi.sensor.sgp30;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;
import java.util.stream.IntStream;

/**
 * Java driver for Adafruit abc.SGP30
 * Note: Due to pi4j this only works with JDK8
 *
 * Courtesy of https://github.com/vkaam/SGP30-Java/blob/master/src/main/java/abc/SGP30.java
 */
public class SGP30i2c {

    static final int[] POSSIBLE_FEATURE_SETS = new int[]{0x0020, 0x0022};
    private int address;
    private int wordLen;
    private int busNo;
    private I2CDevice device;
    private I2CBus bus;
    private int crc8Init;
    private int[] serial;
    private int crc8Polynomial;
    int[] featureset;

    SGP30i2c(int address) {
        this(address, 2, 1, 0xFF, 0x31);
    }

    SGP30i2c(int address, int wordLen, int busNo, int crc8Init, int crc8Polynomial){
        this.address = address; // 0x58
        this.wordLen = wordLen; // 2
        this.busNo = busNo; // 1
        this.crc8Init = crc8Init; // 0xFF;
        this.crc8Polynomial = crc8Polynomial; //  0x31
    }

    void initialize() throws IOException, I2CFactory.UnsupportedBusNumberException, InterruptedException {
        initialiseDevice();
        loadSerial();
        loadFeatureset();
        if (IntStream.of(POSSIBLE_FEATURE_SETS).noneMatch(x -> x == featureset[0])) {
            throw new RuntimeException("Unknown featureset");
        }
    }


    void loadFeatureset() throws IOException, InterruptedException {
        byte[] featuresetCommand = new byte[]{0x20, 0x2F};
        featureset = readWordsFromCommand(featuresetCommand, 10, 1);
    }

    /**
     * Gets the current TVOC value
     *
     * @return the TVOC value
     * @throws IOException          could not read from abc.SGP30
     * @throws InterruptedException interrupted during delay
     */
    public int getTVOC() throws IOException, InterruptedException {
        return iaqMeasure()[1];
    }

    /**
     * Gets the currently active TVOC Baseline
     *
     * @return the TVOC baseline
     * @throws IOException          could not read from abc.SGP30
     * @throws InterruptedException interrupted during delay
     */
    public int getBaseLineTVOC() throws IOException, InterruptedException {
        return iaqBaseLine()[1];
    }

    /**
     * Gets the current eCO2 value
     *
     * @return the eCO2 value
     * @throws IOException          could not read from abc.SGP30
     * @throws InterruptedException interrupted during delay
     */
    public int getECO2() throws IOException, InterruptedException {
        return iaqMeasure()[0];
    }

    /**
     * Gets the currently active eCO2 Baseline
     *
     * @return the eCO2 baseline
     * @throws IOException          could not read from abc.SGP30
     * @throws InterruptedException interrupted during delay
     */
    public int getBaseLineECO2() throws IOException, InterruptedException {
        return iaqBaseLine()[0];
    }

    public int[] getSerial() {
        return serial;
    }

    /**
     * Queries for the currently active baseline values
     *
     * @return the current baselines
     * @throws IOException          could not read from abc.SGP30
     * @throws InterruptedException interrupted during delay
     */
    private int[] iaqBaseLine() throws IOException, InterruptedException {
        return readWordsFromCommand(new byte[]{0x20, 0x15}, 20, 2);
    }

    /**
     * Queries for the current values of eCO2 and TVOC
     *
     * @return the current values of eCO2 and TVOC
     * @throws IOException          could not read from abc.SGP30
     * @throws InterruptedException interrupted during delay
     */
    private int[] iaqMeasure() throws IOException, InterruptedException {
        return readWordsFromCommand(new byte[]{0x20, 0x08}, 50, 2);
    }

    /**
     * @throws IOException          could not read from abc.SGP30
     * @throws InterruptedException interrupted during delay
     */
    void iaqInit() throws IOException, InterruptedException {
        readWordsFromCommand(new byte[]{0x20, 0x03}, 10, 0);
    }

    /**
     * Initialises a new I2C device
     *
     * @throws IOException                              could not get abc.SGP30 instance on I2C bus
     * @throws I2CFactory.UnsupportedBusNumberException wrong bus no
     */
    void initialiseDevice() throws IOException, I2CFactory.UnsupportedBusNumberException {
        bus = I2CFactory.getInstance(busNo);
        device = bus.getDevice(address);
    }

    /**
     * @param command   command to execute over I2C
     * @param delay     delay to wait for read of I2C
     * @param replySize size of the expected reply
     * @return the read result
     * @throws IOException          could not read from abc.SGP30
     * @throws InterruptedException interrupted during delay
     */
    private int[] readWordsFromCommand(byte[] command, int delay, int replySize) throws IOException, InterruptedException {
        //byte[] writeBuffer, int writeOffset, int writeSize, byte[] readBuffer, int readOffset, int readSize
        device.write(command, 0, command.length);
        Thread.sleep(delay);
        int readsize = replySize * (wordLen + 1);
        byte[] crcResponse = new byte[readsize];
        device.read(crcResponse, 0, readsize);
        int[] intResponse = new int[readsize];
        for (int i = 0; i < crcResponse.length; i++) {
            intResponse[i] = crcResponse[i] & 0xFF;
        }
        int[] result = new int[replySize];
        for (int i = 0; i < replySize; i++) {
            int[] word = new int[]{intResponse[3 * i], intResponse[3 * i + 1]};
            int crc = intResponse[3 * i + 2];
            int crcCheck = generateCrc(word);
            if (crcCheck != crc) {
                throw new IOException("CRC error " + crc + " != " + crcCheck + " for crc check " + i);
            }
            result[i] = (word[0] << 8 | word[1]);
        }
        return result;
    }

    /**
     * Generates CRC8 for given data
     *
     * @param data int array to generate CRC8 for
     * @return CRC8 of given data
     */
    private int generateCrc(int[] data) {
        int crc = crc8Init;
        for (int bt :
                data) {
            crc ^= bt;
            for (int i = 0; i < 8; i++) {
                int test = crc & 0x80;
                if (test != 0) {
                    crc = (crc << 1) ^ crc8Polynomial;
                } else {
                    crc <<= 1;
                }
            }
        }
        return crc & 0xFF;
    }

    /**
     * Loads the serial no. of the abc.SGP30 device
     *
     * @throws IOException          could not read from abc.SGP30
     * @throws InterruptedException interrupted during delay
     */
    void loadSerial() throws IOException, InterruptedException {
        byte[] command = new byte[]{0x36, (byte) 0x82};
        serial = readWordsFromCommand(command, 10, 3);
    }

    /**
     * Sets the humidity in g/m3 for eCO2 and TVOC compensation algorithm
     *
     * @param gramsPM3 g/m3 of humidity for compensation
     * @throws IOException          could not read from abc.SGP30
     * @throws InterruptedException interrupted during delay
     */
    // TODO: Unit test
    public void setIaqHumidity(double gramsPM3) throws IOException, InterruptedException {
        // 8.8 bit fixed point conversion
        double maxFixedPoint = 255.0 + (255.0 / 256.0);
        double trimmedFloat = Math.max(0, Math.min(maxFixedPoint, gramsPM3));
        int[] arr = {
                ((int)  trimmedFloat)           & 0xFF,
                ((int) (trimmedFloat * 256.0))  & 0xFF};

        byte[] command = new byte[]{0x20, 0x61, (byte) arr[0], (byte) arr[1], (byte) generateCrc(arr)};
        readWordsFromCommand(command, 10, 0);
    }

    /**
     * Sets the previously IAQ algorithm baseline for eCO2 and TVOC
     *
     * @param eCO2 eCO2 value that has to act as baseline
     * @param TVOC TVOC value that has to act as baseline
     * @throws IOException          could not read from abc.SGP30
     * @throws InterruptedException interrupted during delay
     */
    public void setIaqBaseline(int eCO2, int TVOC) throws IOException, InterruptedException {
        if (eCO2 == 0 || TVOC == 0) {
            throw new RuntimeException("Invalid baseline values");
        }
        int[] arr = new int[2];
        byte[] command = new byte[8];
        command[0] = 0x20;
        command[1] = 0x1E;
        int count = 0;
        for (int value : new int[]{eCO2, TVOC}) {
            arr[0] = value >> 8;
            arr[1] = value & 0xFF;
            command[count * 3 + 2] = (byte) arr[0];
            command[count * 3 + 3] = (byte) arr[1];
            command[count * 3 + 4] = (byte) generateCrc(arr);
            count++;
        }
        readWordsFromCommand(command, 10, 0);
    }

    public void resetSGP30() throws IOException, InterruptedException {
        byte[] command = new byte[]{0x0006};
        readWordsFromCommand(command, 10, 0);
    }
}
