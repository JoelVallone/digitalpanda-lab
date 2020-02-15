CREATE KEYSPACE IF NOT EXISTS iot WITH durable_writes = true AND replication = { 'replication_factor' : 1, 'class' : 'SimpleStrategy' };


CREATE TABLE IF NOT EXISTS iot.sensor_measure_latest (
   location text,
   measure_type text,
   timestamp timestamp,
   value double,
   primary key((location), measure_type)
) WITH COMPACTION = {'class': 'LeveledCompactionStrategy', 'tombstone_compaction_interval': '14400'};


CREATE TABLE IF NOT EXISTS iot.sensor_measure_history_seconds (
    location text,
    time_block_id bigint,
    measure_type text,
    bucket int,
    timestamp timestamp,
    value double,
    primary key((location, time_block_id, measure_type, bucket), timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);


CREATE TABLE IF NOT EXISTS iot.sensor_measure_history_seconds_60 (
    location text,
    time_block_id bigint,
    measure_type text,
    bucket int,
    timestamp timestamp,
    value double,
    primary key((location, time_block_id, measure_type, bucket), timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);

CREATE TABLE IF NOT EXISTS iot.sensor_measure_history_seconds_3600 (
    location text,
    time_block_id bigint,
    measure_type text,
    bucket int,
    timestamp timestamp,
    value double,
    primary key((location, time_block_id, measure_type, bucket), timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);


CREATE TABLE IF NOT EXISTS iot.sensor_measure_history_seconds_86400 (
    location text,
    time_block_id bigint,
    measure_type text,
    bucket int,
    timestamp timestamp,
    value double,
    primary key((location, time_block_id, measure_type, bucket), timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);

INSERT INTO iot.sensor_measure_history_seconds (location, time_block_id, measure_type, bucket, timestamp, value) VALUES
    ( 'server-room', 2979, 'TEMPERATURE', 0, '2019-06-30 22:09:59.928+00:00', 26);

INSERT INTO iot.sensor_measure_history_seconds (location, time_block_id, measure_type, bucket, timestamp, value) VALUES
    ( 'server-room', 2979, 'TEMPERATURE', 0, '2019-06-30 22:09:58.927+00:00', 26);

INSERT INTO iot.sensor_measure_history_seconds (location, time_block_id, measure_type, bucket, timestamp, value) VALUES
    ( 'server-room', 2979, 'TEMPERATURE', 0, '2019-06-30 22:09:57.926+00:00', 26);

INSERT INTO iot.sensor_measure_history_seconds (location, time_block_id, measure_type, bucket, timestamp, value) VALUES
    ( 'server-room', 2979, 'TEMPERATURE', 0, '2019-06-30 22:09:56.926+00:00', 27);

INSERT INTO iot.sensor_measure_history_seconds (location, time_block_id, measure_type, bucket, timestamp, value) VALUES
    ( 'server-room', 2979, 'TEMPERATURE', 0, '2019-06-30 22:09:55.926+00:00', 27);

INSERT INTO iot.sensor_measure_history_seconds (location, time_block_id, measure_type, bucket, timestamp, value) VALUES
    ( 'server-room', 2979, 'TEMPERATURE', 0, '2019-06-30 22:09:54.925+00:00', 27);

INSERT INTO iot.sensor_measure_history_seconds (location, time_block_id, measure_type, bucket, timestamp, value) VALUES
    ( 'server-room', 2979, 'TEMPERATURE', 0, '2019-06-30 22:09:53.926+00:00', 26.5);

INSERT INTO iot.sensor_measure_history_seconds (location, time_block_id, measure_type, bucket, timestamp, value) VALUES
    ( 'server-room', 2979, 'TEMPERATURE', 0, '2019-06-30 22:09:52.926+00:00', 26.5);

INSERT INTO iot.sensor_measure_history_seconds (location, time_block_id, measure_type, bucket, timestamp, value) VALUES
    ( 'server-room', 2979, 'TEMPERATURE', 0, '2019-06-30 22:09:51.926+00:00', 26.5);

INSERT INTO iot.sensor_measure_history_seconds (location, time_block_id, measure_type, bucket, timestamp, value) VALUES
    ( 'server-room', 2979, 'TEMPERATURE', 0, '2019-06-30 22:19:50.926+00:00', 40.0);

INSERT INTO iot.sensor_measure_history_seconds (location, time_block_id, measure_type, bucket, timestamp, value) VALUES
    ( 'server-room', 2979, 'TEMPERATURE', 0, '2019-06-30 22:20:00.926+00:00', 300000.5);


INSERT INTO iot.sensor_measure_history_seconds (location, time_block_id, measure_type, bucket, timestamp, value) VALUES
    ( 'server-room', 2979, 'PRESSURE'   , 0, '2019-06-30 22:09:50.926+00:00', 600000.5);


INSERT INTO iot.sensor_measure_history_seconds (location, time_block_id, measure_type, bucket, timestamp, value) VALUES
    ( 'outdoor',     2979, 'TEMPERATURE', 0, '2019-06-30 22:09:50.926+00:00', 27.5);



INSERT INTO iot.sensor_measure_latest (location, measure_type, timestamp, value) VALUES
    ( 'server-room',   'PRESSURE', '2019-06-30 22:09:50.926+00:00', 966.5);

INSERT INTO iot.sensor_measure_latest (location, measure_type, timestamp, value) VALUES
    ( 'server-room',   'TEMPERATURE', '2019-06-30 22:09:50.926+00:00', 23.3);

INSERT INTO iot.sensor_measure_latest (location, measure_type, timestamp, value) VALUES
    ( 'outdoor',       'HUMIDITY', '2019-06-30 22:09:50.926+00:00', 75.33896);

INSERT INTO iot.sensor_measure_latest (location, measure_type, timestamp, value) VALUES
    ( 'outdoor',       'PRESSURE', '2019-06-30 22:09:50.926+00:00', 967.72356);

INSERT INTO iot.sensor_measure_latest (location, measure_type, timestamp, value) VALUES
    ( 'outdoor',       'TEMPERATURE', '2019-06-30 22:09:50.926+00:00', 23.7739);
