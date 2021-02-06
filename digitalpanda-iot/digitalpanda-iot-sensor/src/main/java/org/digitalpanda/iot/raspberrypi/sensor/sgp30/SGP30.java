package org.digitalpanda.iot.raspberrypi.sensor.sgp30;

import com.google.gson.*;
import com.pi4j.io.i2c.I2CFactory;
import org.apache.commons.io.FileUtils;
import org.digitalpanda.common.data.backend.SensorMeasureType;
import org.digitalpanda.common.data.backend.SensorMeasures;
import org.digitalpanda.iot.raspberrypi.Configuration;
import org.digitalpanda.iot.raspberrypi.sensor.Sensor;
import org.digitalpanda.iot.raspberrypi.sensor.SensorData;
import org.digitalpanda.iot.raspberrypi.sensor.SensorModel;
import org.digitalpanda.iot.raspberrypi.sensor.utils.SensorDataMapper;
import org.digitalpanda.iot.raspberrypi.sensor.utils.UnitConversion;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * SGP30 eCO2 and TVO gas sensor wrapper class handling:
 *  * The complex SGP30 state management :
 *      1) periodic humidity update for the sensor calibration
 *      2) initialise the baseline eCO2 and TVOC from a file if available and fresh enough (not older than 7 days)
 *          The baseline values are necessary for proper sensor output (smoothing with historical average)
 *      3) periodically dump the baseline eCO2 and TVOC once available.
 *          If the baseline data from the file is not available the baseline eCO2 and TVOC
 *          are initialized after running the IAQ algorithm for 12 hours.
 *  * Interface the low-level SGP30 i2c register accesses with the sensor application abstractions
 */
public class SGP30 implements Sensor {

    private String jsonStateFilePath;
    private static final String DEFAULT_JSON_STATE_FILE_PATH  = "./sgp30-state.json";
    private long lastHumidityRefreshMillis;
    private final long humidityIaqRefreshPeriodMillis;
    static final long HUMIDITY_IAQ_REFRESH_PERIOD_MILLIS  = TimeUnit.MINUTES.toMillis(10);


    private long baselineInitStartMillis;
    private final long baselineInitDelayMillis;
    static final long BASELINE_INIT_DELAY_MILLIS  = TimeUnit.HOURS.toMillis(12);

    private Integer fileInitECO2Baseline;
    private Integer fileInitTVOCBaseline;
    private long lastBaselineDumpToFileMillis;
    private final long baselineFileDumpRefreshPeriodMillis;
    static final long BASELINE_FILE_DUMP_REFRESH_PERIOD_MILLIS  = TimeUnit.MINUTES.toMillis(10);
    private final long fileBaselineValidityDelayMillis;
    static final long FILE_BASELINE_VALIDITY_DELAY_MILLIS = TimeUnit.DAYS.toMillis(7);

    private SensorData sgp30Data;
    private SGP30i2c sgp30i2c;
    public final static int SGP30_I2C_DEVICE_ADDRESS = 0x58;

    private boolean initialized;

    public SGP30() {
        this(DEFAULT_JSON_STATE_FILE_PATH, null);
    }

    public SGP30(String sensorSgp30JsonStateFilePath, SGP30i2c sgp30i2c) {
        this(sensorSgp30JsonStateFilePath,
                HUMIDITY_IAQ_REFRESH_PERIOD_MILLIS,
                BASELINE_FILE_DUMP_REFRESH_PERIOD_MILLIS,
                BASELINE_INIT_DELAY_MILLIS,
                FILE_BASELINE_VALIDITY_DELAY_MILLIS, sgp30i2c);
    }

    /**
     * Main constructor with file based configuration
     *
     * @param conf Configuration with optional config Configuration.ConfigurationKey.SENSOR_SGP30_JSON_STATE_FILE_PATH
     */
    public SGP30(Configuration conf) {
        this(Optional
                .of(conf.getString(Configuration.ConfigurationKey.SENSOR_SGP30_JSON_STATE_FILE_PATH))
                .orElse(DEFAULT_JSON_STATE_FILE_PATH),
                HUMIDITY_IAQ_REFRESH_PERIOD_MILLIS,
                BASELINE_FILE_DUMP_REFRESH_PERIOD_MILLIS,
                BASELINE_INIT_DELAY_MILLIS,
                FILE_BASELINE_VALIDITY_DELAY_MILLIS,
                null);
    }

    SGP30(String sensorSgp30JsonStateFilePath,
         long humidityIaqRefreshPeriodMillis,
         long baselineFileDumpRefreshPeriodMillis,
         long baselineInitDelayMillis,
         long fileBaselineValidityDelayMillis,
          SGP30i2c sgp30i2c) {
        // Sensor state management timings
        this.jsonStateFilePath = sensorSgp30JsonStateFilePath;
        this.humidityIaqRefreshPeriodMillis = humidityIaqRefreshPeriodMillis;
        this.baselineFileDumpRefreshPeriodMillis = baselineFileDumpRefreshPeriodMillis;
        this.lastBaselineDumpToFileMillis = 0L;
        this.baselineInitDelayMillis = baselineInitDelayMillis;
        this.fileBaselineValidityDelayMillis = fileBaselineValidityDelayMillis;
        this.initialized = false;

        // Delegate low-level sensor interfacing
        this.sgp30i2c = sgp30i2c == null ? new SGP30i2c(SGP30_I2C_DEVICE_ADDRESS): sgp30i2c;
    }

    @Override
    public boolean initialize() {
        try {
            sgp30i2c.initialize();
            iaqInit();
            this.initialized = true;
        } catch (I2CFactory.UnsupportedBusNumberException | IOException | InterruptedException e) {
            e.printStackTrace();
            this.initialized = false;
        }
        return initialized;
    }

    private void iaqInit() throws IOException, InterruptedException {
        if (tryLoadBaselineFromFile()) {
            sgp30i2c.setIaqBaseline(fileInitECO2Baseline, fileInitTVOCBaseline);
        }
        sgp30i2c.iaqInit();
        baselineInitStartMillis = System.currentTimeMillis();
    }

    @Override
    public void calibrate(List<SensorMeasures> allLatestAvailableMeasures) {
        if ((lastHumidityRefreshMillis + humidityIaqRefreshPeriodMillis) < (System.currentTimeMillis())) {
            tryComputeHumidityGramsPerCubicMeter(allLatestAvailableMeasures)
                    .ifPresent( humidityGramsPerCubicMeter -> {
                        try {
                            sgp30i2c.setIaqHumidity(humidityGramsPerCubicMeter);
                            lastHumidityRefreshMillis = System.currentTimeMillis();
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

    static Optional<Double> tryComputeHumidityGramsPerCubicMeter(List<SensorMeasures> measures) {
        Double tempCelsius = SensorDataMapper.latestValueOf(measures, SensorMeasureType.TEMPERATURE).orElse(null);
        Double relativeHumidity = SensorDataMapper.latestValueOf(measures, SensorMeasureType.HUMIDITY).orElse(null);
        if (tempCelsius != null && relativeHumidity != null) {
            return UnitConversion.tryComputeHumidityGramsPerCubicMeter(tempCelsius, relativeHumidity);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public SensorData fetchAndComputeValues() throws IOException, InterruptedException {
        if (!initialized) {
            if (!initialize()) {
                return null;
            }
        }

        maybeSaveBaseline();

        this.sgp30Data =  (new SensorData(SensorModel.SGP30))
                .setSensorData(SensorMeasureType.eCO2, sgp30i2c.getECO2())
                .setSensorData(SensorMeasureType.TVOC, sgp30i2c.getTVOC());
        return sgp30Data;
    }

    void maybeSaveBaseline() throws IOException, InterruptedException {
        if (isBaselineAvailable()
                && (lastBaselineDumpToFileMillis + baselineFileDumpRefreshPeriodMillis < System.currentTimeMillis())) {
            String baselineStateJson =
                    String.format("{\n" +
                    "  \"eco2Baseline\": %d,\n" +
                    "  \"tvocBaseline\": %d,\n" +
                    "  \"baselineIsoOffsetDateTime\": \"%s\"\n" +
                    "}\n",
                        sgp30i2c.getBaseLineECO2(),
                        sgp30i2c.getBaseLineTVOC(),
                        ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            try {
                File file = new File(jsonStateFilePath);
                FileUtils.write(file, baselineStateJson, Charset.defaultCharset());
                lastBaselineDumpToFileMillis = System.currentTimeMillis();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    boolean tryLoadBaselineFromFile() {
        File file = new File(jsonStateFilePath);
        if (file.exists()) {
            try {
                String jsonString = FileUtils.readFileToString(file, Charset.defaultCharset());
                JsonObject jsonTree = (new JsonParser()).parse(jsonString).getAsJsonObject();
                fileInitECO2Baseline = Optional.ofNullable(jsonTree.get("eco2Baseline")).map(JsonElement::getAsInt).orElse(null);
                fileInitTVOCBaseline = Optional.ofNullable(jsonTree.get("tvocBaseline")).map(JsonElement::getAsInt).orElse(null);
                lastBaselineDumpToFileMillis = Optional
                        .ofNullable(jsonTree.get("baselineIsoOffsetDateTime"))
                        .map(JsonElement::getAsString)
                        .map( timeAsString -> ZonedDateTime.parse(timeAsString, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant().toEpochMilli())
                        .orElse(null);
            } catch (Exception e) {
                e.printStackTrace();
                lastBaselineDumpToFileMillis = 0L;
            }
        }
        boolean isBaselineFromFileAvailable = isBaselineFromFileAvailable();
        if (!isBaselineFromFileAvailable) {
            fileInitECO2Baseline = null;
            fileInitTVOCBaseline = null;
            lastBaselineDumpToFileMillis = 0L;
        }
        return isBaselineFromFileAvailable;
    }

    private boolean isBaselineAvailable() {
        long baselineFullInitReadyTimeMillis = baselineInitStartMillis + baselineInitDelayMillis;
        return (baselineFullInitReadyTimeMillis < System.currentTimeMillis()) || isBaselineFromFileAvailable();
    }

    private boolean isBaselineFromFileAvailable() {
        long baselineValidityEndMillis = lastBaselineDumpToFileMillis + fileBaselineValidityDelayMillis;
        return baselineValidityEndMillis > System.currentTimeMillis()
                && fileInitECO2Baseline != null && fileInitTVOCBaseline != null;
    }

    @Override
    public SensorData getLastRecord() {
        return sgp30Data;
    }

    Integer getFileInitECO2Baseline() {
        return fileInitECO2Baseline;
    }

    Integer getFileInitTVOCBaseline() {
        return fileInitTVOCBaseline;
    }

    public Long getLastBaselineDumpToFileMillis() {
        return lastBaselineDumpToFileMillis;
    }


}
