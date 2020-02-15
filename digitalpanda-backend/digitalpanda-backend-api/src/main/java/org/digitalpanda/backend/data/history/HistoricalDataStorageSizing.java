package org.digitalpanda.backend.data.history;

import java.util.Arrays;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/*
Web user : < 200 ms responsiveness
    -> api call :   metadata = (start millis (9 Bytes), end millis (9 Bytes), aggregate type (20 Bytes), aggregate interval millis (9 Bytes), measure type (20 Bytes));
                    data = List of [0-9]{5}.[0-9]{4} (10 Bytes) values
    With 3G basic connectivity 300 KiB/s => 60 KiB payload
        => Max 6 K measures for any interval
        => Throttle request size on client side,
            If needed, recompute aggregates in backend service,
            Historical data in DB can be pre/batch-computed but obeys Cassandra constraints :
                -> Evenly sized partitions, target partition size, time-ordered insertions, dynamic TTL ...

Cassandra: Maximum partition size : 50 MiB,
    -> sensor_measure_history row max size : 100 Bytes
    => 500 k records
*/
public enum HistoricalDataStorageSizing {
    //Ordered by increasing aggregate interval.
    SECOND_PRECISION_RAW(1L, AggregateType.VALUE), //Raw data provided by sensor network
    MINUTE_PRECISION_AVG(60L, AggregateType.AVERAGE),
    HOUR_PRECISION_AVG(3600L, AggregateType.AVERAGE),
    DAY_PRECISION_AVG(24*3600L, AggregateType.AVERAGE);

    public static final long MAX_TABLE_PARTITION_SIZE_BYTES = 50 * (1L << 20);
    public static final long MEASURE_HISTORY_ROW_SIZE_BYTES = 100L;
    public static final long RECORDS_PER_TABLE_PARTITION  = (MAX_TABLE_PARTITION_SIZE_BYTES / MEASURE_HISTORY_ROW_SIZE_BYTES);

    private Long timeBlockPeriodSeconds;
    private Long aggregateIntervalSeconds;
    private AggregateType aggregateType;

    private static final Map<Long, HistoricalDataStorageSizing> intervalMap = Arrays
            .stream(HistoricalDataStorageSizing.values())
            .collect(toMap(HistoricalDataStorageSizing::getAggregateIntervalSeconds, identity()));


    HistoricalDataStorageSizing(Long aggregateIntervalSeconds, AggregateType aggregateType){
        this.aggregateIntervalSeconds = aggregateIntervalSeconds;
        this.timeBlockPeriodSeconds = aggregateIntervalSeconds * RECORDS_PER_TABLE_PARTITION;
        this.aggregateType = aggregateType;
    }

    public Long getTimeBlockPeriodSeconds() {
        return timeBlockPeriodSeconds;
    }

    public Long getAggregateIntervalSeconds() {
        return aggregateIntervalSeconds;
    }

    public AggregateType getAggregateType() {
        return aggregateType;
    }

    public static HistoricalDataStorageSizing fromIntervalSeconds(long intervalSec) {
        return intervalMap.get(intervalSec);
    }
}
