package org.digitalpanda.backend.data.history;

import org.digitalpanda.backend.data.SensorMeasureType;

import java.util.List;
import java.util.stream.LongStream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class HistoricalDataStorageHelper {

    public static final int SENSOR_MEASURE_DEFAULT_BUCKET_ID = 0;

    public static long getHistoricalMeasureBlockId(long targetTimeMillis, HistoricalDataStorageSizing targetHistoricalData) {
        return targetTimeMillis / 1000L / targetHistoricalData.getTimeBlockPeriodSeconds();
    }

    public static String cqlTableOf(HistoricalDataStorageSizing sizing) {
        switch (sizing) {
            case SECOND_PRECISION_RAW:
                return "sensor_measure_history_seconds";
            default:
                return "sensor_measure_history_seconds_" + sizing.getAggregateIntervalSeconds();
        }
    }

    public static List<String> getRangeSelectionCqlQueries(
            String location,
            SensorMeasureType measureType,
            HistoricalDataStorageSizing targetHistoricalDataSizing,
            long intervalBeginMillisIncl,
            long intervalEndSecondsIncl) {

        String tableWithTargetGranularity = cqlTableOf(targetHistoricalDataSizing);
        long startBlockId = getHistoricalMeasureBlockId(intervalBeginMillisIncl, targetHistoricalDataSizing);
        long endBlockId = getHistoricalMeasureBlockId(intervalEndSecondsIncl, targetHistoricalDataSizing);

        return LongStream.rangeClosed(startBlockId, endBlockId).boxed()
                .map(blockId ->
                        String.format(
                            "SELECT * FROM iot.%s WHERE " +
                                    "location = '%s' AND " +
                                    "time_block_id = %d AND " +
                                    "measure_type = '%s' AND " +
                                    "bucket = %d AND " +
                                    "timestamp >= %d AND timestamp <= %d",
                            tableWithTargetGranularity,
                            location,
                            blockId,
                            measureType.name(),
                            SENSOR_MEASURE_DEFAULT_BUCKET_ID,
                            intervalBeginMillisIncl,
                            intervalEndSecondsIncl)
                )
                .collect(toList());
    }
}
