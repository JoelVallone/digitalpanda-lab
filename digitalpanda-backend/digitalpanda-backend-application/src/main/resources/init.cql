CREATE KEYSPACE IF NOT EXISTS iot
WITH durable_writes = true
AND replication = { 'replication_factor' : 1, 'class' : 'SimpleStrategy' };

/*
    Goal : Get all the ordered measures for a location with a compromise between intervals size and measure aggregate granularity
        Compaction : TWCS
            > Allows to compact rows into the same SS table with insertion time locality
            > Only appropriate for strictly ordered time series insertion
        Notes:
            > Table structure scales with the number of locations, measure type, aggregate types,
            > Time block allows to compute aggregates with differezoont granularities allowing variable interval size
            > The writes may happen in batch or continuously but are ALWAYS time-ordered

    CREATE TABLE IF NOT EXISTS iot.sensor_aggregate_history
    ... same as sensor_measure_history_seconds and add aggregate_type text as partition key

    Goal : Get all the ordered measures for a location with similar measure frequency
        Compaction : TWCS
            > The writes happen continuously the same frequency and are ALWAYS time-ordered
*/
    CREATE TABLE IF NOT EXISTS iot.sensor_measure_history_seconds (
        location text,
        time_block_id bigint,
        measure_type text,
        bucket int,
        timestamp timestamp,
        value double,
        primary key((location, time_block_id, measure_type, bucket), timestamp)
    ) WITH CLUSTERING ORDER BY (timestamp DESC)
              AND COMPACTION = {'class': 'TimeWindowCompactionStrategy',
                            'compaction_window_unit': 'DAYS',
                            'compaction_window_size': 7}
              AND default_time_to_live = 94608000; -- 3 * 365 * 24 * 3600

/*
    Goal: Get the all the latest measure of any measure identified by location and type
        Compaction : LCS
            > Is read optimized and guarantees efficient data compaction
            > HIGH SStable compaction background cost (CPU time).
*/
CREATE TABLE IF NOT EXISTS iot.sensor_measure_latest (
   location text,
   measure_type text,
   timestamp timestamp,
   value double,
   primary key((location), measure_type)
) WITH COMPACTION = {'class': 'LeveledCompactionStrategy',
                      'tombstone_compaction_interval': '14400'};