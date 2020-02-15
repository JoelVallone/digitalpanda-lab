CREATE TABLE IF NOT EXISTS iot.sensor_measure_history_seconds_60 (
    location text,
    measure_type text,
    timestamp timestamp,
    value double,
    primary key((location, time_block_id, measure_type, bucket), timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC)
          AND COMPACTION = {'class': 'TimeWindowCompactionStrategy',
                        'compaction_window_unit': 'DAYS',
                        'compaction_window_size': 365}
          AND default_time_to_live = 5676480000; -- 180 years