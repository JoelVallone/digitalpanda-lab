package org.digitalpanda.backend.application.northbound.service;

import org.digitalpanda.backend.application.util.Pair;
import org.digitalpanda.backend.data.history.HistoricalDataStorageSizing;
import org.digitalpanda.backend.application.persistence.measure.history.SensorMeasureHistorySecondsDao;
import org.digitalpanda.backend.application.persistence.measure.history.SensorMeasureHistoryRepository;
import org.digitalpanda.backend.data.SensorMeasureMetaData;
import org.digitalpanda.backend.data.SensorMeasureType;
import org.digitalpanda.backend.data.SensorMeasures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.toIntExact;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.digitalpanda.backend.application.persistence.measure.history.SensorMeasureHistorySecondsDao.ROW_SIZE_BYTES;
import static org.digitalpanda.backend.data.history.HistoricalDataStorageHelper.SENSOR_MEASURE_DEFAULT_BUCKET_ID;
import static org.digitalpanda.backend.data.history.HistoricalDataStorageHelper.getHistoricalMeasureBlockId;
import static org.digitalpanda.backend.data.history.HistoricalDataStorageSizing.SECOND_PRECISION_RAW;

@Service
public class SensorMeasureHistoryService {

    private boolean allowAggregates;
    private Logger logger = LoggerFactory.getLogger(SensorMeasureHistoryService.class);

    public static final double MAX_OUTPUT_MEASURES_JITTER = 1.2;
    public static final int MAX_ROW_COUNT = 55*1000*1000 / ROW_SIZE_BYTES; //~55 MiB of data ~= 581'395 data points with second period ~= 7.4 days sample size !

    private SensorMeasureHistoryRepository sensorMeasureHistoryRepository;

    @Autowired
    public SensorMeasureHistoryService(
            SensorMeasureHistoryRepository sensorMeasureHistoryRepository,
            @Value("${digitalpanda.sensor.allowAggregates}") boolean allowAggregates) {
        this.sensorMeasureHistoryRepository = sensorMeasureHistoryRepository;
        this.allowAggregates = allowAggregates;
    }

    public void saveAllSecondPrecisionMeasures(List<SensorMeasures> sensorMeasuresList){
        sensorMeasureHistoryRepository.saveAllSecondPrecisionMeasures(
                sensorMeasuresList.stream()
                .map(this::toSensorMeasuresSecondDao)
                .flatMap(List::stream)
                .collect(Collectors.toList())
        );
    }

    private List<SensorMeasureHistorySecondsDao> toSensorMeasuresSecondDao(SensorMeasures sensorMeasures){
        SensorMeasureMetaData sensorMeasureMetaData = sensorMeasures.getSensorMeasureMetaData();
        if(sensorMeasureMetaData == null ||
                sensorMeasureMetaData.getType() == null ||
                sensorMeasureMetaData.getLocation() == null ||
                sensorMeasureMetaData.getLocation().isEmpty() ||
                sensorMeasures.getMeasures() == null ){
            logger.warn("Malformed sensor measures: " + sensorMeasures.getSensorMeasureMetaData());
            return emptyList();
        }
        return sensorMeasures.getMeasures().stream().map(sensorMeasure -> {
            SensorMeasureHistorySecondsDao dao = new SensorMeasureHistorySecondsDao();
            dao.setLocation(sensorMeasureMetaData.getLocation()); //Partition field
            dao.setTimeBlockId(getHistoricalMeasureBlockId(sensorMeasure.getTimestamp(), SECOND_PRECISION_RAW)); //Partition field
            dao.setMeasureType(sensorMeasureMetaData.getType().name()); //Partition field
            dao.setBucket(SENSOR_MEASURE_DEFAULT_BUCKET_ID); //Partition field
            dao.setTimestamp(Date.from(Instant.ofEpochMilli(sensorMeasure.getTimestamp())));//Clustering field
            dao.setValue(sensorMeasure.getValue());
            return dao;
        }).collect(Collectors.toList());
    }

    public List<SensorMeasuresEquidistributed> getMeasuresWithContinuousEquidistributedSubIntervals(
            String location, SensorMeasureType sensorMeasureType, long startTimeMillisIncl, long endTimeMillisExcl, int dataPointCount) {

        long start = System.currentTimeMillis();
        Pair<HistoricalDataStorageSizing, List<SensorMeasureHistorySecondsDao>> timeSortedBaseSample =
                loadBestFittingSample(
                        location,
                        sensorMeasureType,
                        startTimeMillisIncl,
                        endTimeMillisExcl,
                        dataPointCount);
        long dbLoaded = System.currentTimeMillis();

        List<SensorMeasuresEquidistributed> resizedSample =
                resizeSample(
                        startTimeMillisIncl,
                        endTimeMillisExcl,
                        dataPointCount,
                        timeSortedBaseSample.getSecond(),
                        timeSortedBaseSample.getFirst());
        long sampleResized = System.currentTimeMillis();

        logger.info("Execution time breakdown:"
                        + "\n> Total execution time: " + (sampleResized - start) + " Millis"
                        + "\n -> db load time: " + (dbLoaded - start) + " Millis"
                        + "\n --> row count: " + timeSortedBaseSample.getSecond().size()
                        + "\n -> sample resizing: " + (sampleResized - dbLoaded) + " Millis"
                        + "\n --> Covered sample interval: " + (endTimeMillisExcl - startTimeMillisIncl) + " Millis"
        );

        return resizedSample;
    }

    Pair<HistoricalDataStorageSizing, List<SensorMeasureHistorySecondsDao>> loadBestFittingSample(
            String location, SensorMeasureType sensorMeasureType, long startTimeMillisIncl, long endTimeMillisExcl, int dataPointCount){

        HistoricalDataStorageSizing idealSizing = allowAggregates ? sizingWithLowestFittingPeriod(startTimeMillisIncl / 1000L, endTimeMillisExcl / 1000L, dataPointCount) : SECOND_PRECISION_RAW;

        List<SensorMeasureHistorySecondsDao> sample = emptyList();
        List<HistoricalDataStorageSizing> nextBestSizing =
                Arrays.stream(HistoricalDataStorageSizing.values())
                        .filter(size -> size.ordinal() <= idealSizing.ordinal())
                        .sorted((s1, s2) -> -1 * (s1.ordinal() - s2.ordinal())) // biggest granularity first
                        .collect(toList());
        HistoricalDataStorageSizing actualSizing = idealSizing;
        while (sample.size() == 0 && nextBestSizing.size() > 0) {
            actualSizing = nextBestSizing.get(0);

            // Capping db query response size with time range limitation
            long trimmedEndTimeMillisIncl = endTimeMillisExcl;
            if((endTimeMillisExcl - startTimeMillisIncl) / (actualSizing.getAggregateIntervalSeconds()*1000)  > MAX_ROW_COUNT) {
                trimmedEndTimeMillisIncl = startTimeMillisIncl + (actualSizing.getAggregateIntervalSeconds() * 1000 * MAX_ROW_COUNT);
            }

            sample = loadMeasuresIncreasingOrder(location, sensorMeasureType, startTimeMillisIncl, trimmedEndTimeMillisIncl, actualSizing);
            nextBestSizing = nextBestSizing.size() > 1 ? nextBestSizing.subList(1, nextBestSizing.size()) : emptyList();
            logger.debug("Granularity for history query: "
                    + "\n -> target granularity: " + actualSizing.name()
                    + "\n -> location: " + location
                    + "\n -> measure: " + sensorMeasureType
                    + "\n -> start time: " + startTimeMillisIncl + " Millis"
                    + "\n -> end time: " + endTimeMillisExcl + " Millis"
                    + "\n -> Covered sample interval: " + (endTimeMillisExcl - startTimeMillisIncl) + " Millis");
        }

        return new Pair<>(actualSizing, sample);
    }

    HistoricalDataStorageSizing sizingWithLowestFittingPeriod(long intervalBeginSecondsIncl, long intervalEndSecondsIncl, int dataPointCount) {
        HistoricalDataStorageSizing bestSizing = SECOND_PRECISION_RAW;
        long intervalToSample = intervalEndSecondsIncl - intervalBeginSecondsIncl;
        long minAggregateDataPoints = intervalToSample / bestSizing.getAggregateIntervalSeconds();

        for( HistoricalDataStorageSizing sizing: HistoricalDataStorageSizing.values()) {
            long aggregateInterval  = sizing.getAggregateIntervalSeconds();
            long aggregateDataPoints = intervalToSample / aggregateInterval;
            if ( aggregateDataPoints > 0
                    && aggregateDataPoints > dataPointCount
                    && aggregateDataPoints < minAggregateDataPoints) {
                minAggregateDataPoints = aggregateDataPoints;
                bestSizing = sizing;
            }
        }
        return bestSizing;
    }

    private List<SensorMeasuresEquidistributed> resizeSample(long startTimeMillisIncl, long endTimeMillisExcl, int targetDataPointCount, List<SensorMeasureHistorySecondsDao> storedMeasuresTimeIncreasing, HistoricalDataStorageSizing historicalDataStorageSizing) {
        if(storedMeasuresTimeIncreasing.size() == 0)
            return emptyList();

        final long targetPeriodMillis = (endTimeMillisExcl - startTimeMillisIncl) / targetDataPointCount;
        if (targetPeriodMillis < 1000)
            throw new RuntimeException("Historical data sample bellow second period is not supported");

        long storageDataSampleUnitPeriodMillis = historicalDataStorageSizing.getAggregateIntervalSeconds() * 1000;
        List<SensorMeasuresEquidistributed> sensorMeasuresResizedEquidistributedSubSamples = new ArrayList<>();
        List<SensorMeasureHistorySecondsDao> currentSubIntervalMeasures = new ArrayList<>();
        long curentSubSampleStartIntervalMillis = storedMeasuresTimeIncreasing.get(0).getTimestamp().getTime();

        SensorMeasureHistorySecondsDao previousMeasure = storedMeasuresTimeIncreasing.get(0);

        for (SensorMeasureHistorySecondsDao currentMeasure : storedMeasuresTimeIncreasing) {
            //If the next measure creates a discontinuity:
            // 1) Perform resizing with the accumulated sub sample of data points
            // 2) Start a new sub sample interval
            if (currentMeasure.getTimestamp().getTime() > previousMeasure.getTimestamp().getTime() + (long) (targetPeriodMillis * MAX_OUTPUT_MEASURES_JITTER)) {
                sensorMeasuresResizedEquidistributedSubSamples.add(
                        resizeSubSampleToTargetDataPointPeriodWithAverage(
                            curentSubSampleStartIntervalMillis,
                            previousMeasure.getTimestamp().getTime(), // + storageDataSampleUnitPeriodMillis,
                            targetPeriodMillis,
                            currentSubIntervalMeasures
                ));

                curentSubSampleStartIntervalMillis = currentMeasure.getTimestamp().getTime();
                currentSubIntervalMeasures = new ArrayList<>();
            }

            currentSubIntervalMeasures.add(currentMeasure);
            previousMeasure = currentMeasure;
        }

        if(sensorMeasuresResizedEquidistributedSubSamples.size() == 0 ||
                sensorMeasuresResizedEquidistributedSubSamples.get(sensorMeasuresResizedEquidistributedSubSamples.size()-1).getStartTimeMillisIncl() != curentSubSampleStartIntervalMillis){
            sensorMeasuresResizedEquidistributedSubSamples.add(
                    resizeSubSampleToTargetDataPointPeriodWithAverage(
                            curentSubSampleStartIntervalMillis,
                            previousMeasure.getTimestamp().getTime(), // + storageDataSampleUnitPeriodMillis,
                            targetPeriodMillis,
                            currentSubIntervalMeasures
                    ));
        }

        return sensorMeasuresResizedEquidistributedSubSamples;
    }


    private SensorMeasuresEquidistributed resizeSubSampleToTargetDataPointPeriodWithAverage(
            long startTimeMillisIncl, long endTimeMillisExcl, long targetPeriodMillis, List<SensorMeasureHistorySecondsDao> continuousStorageValuesTimeIncreasing){

        int targetDataPointCount = toIntExact((endTimeMillisExcl - startTimeMillisIncl) / targetPeriodMillis) + 1;
        List<Double> resizedSample = new ArrayList<>(targetDataPointCount);
        long nextPeriodStartTimeMillisIncl = startTimeMillisIncl + targetPeriodMillis;
        double accumulator = 0.0;
        int accCount = 0;
        for (SensorMeasureHistorySecondsDao sensorMeasureDao : continuousStorageValuesTimeIncreasing) {
            if (sensorMeasureDao.getTimestamp().getTime() >= nextPeriodStartTimeMillisIncl) {
                nextPeriodStartTimeMillisIncl += targetPeriodMillis;
                if (accCount != 0) {
                    resizedSample.add(accumulator / accCount);
                } else {
                    resizedSample.add(0.0);
                }
                accCount = 0;
                accumulator = 0.0;
            }

            accumulator += sensorMeasureDao.getValue();
            accCount++;
        }
        if (accCount != 0) {
            resizedSample.add(accumulator / accCount);
        }

        return new SensorMeasuresEquidistributed(startTimeMillisIncl, startTimeMillisIncl + resizedSample.size() * targetPeriodMillis, targetPeriodMillis, resizedSample);
    }


    private List<SensorMeasureHistorySecondsDao> loadMeasuresIncreasingOrder(
            String location, SensorMeasureType sensorMeasureType, long startTimeMillisIncl, long endTimeMillisIncl, HistoricalDataStorageSizing storageSizingWithNearestPeriod) {
        return sensorMeasureHistoryRepository
                .getMeasuresAtLocationWithInterval(
                        location,
                        sensorMeasureType,
                        storageSizingWithNearestPeriod,
                        startTimeMillisIncl,
                        endTimeMillisIncl).stream()
                .sorted(Comparator.comparingLong(sensorMeasureHistoryDao -> sensorMeasureHistoryDao.getTimestamp().getTime()))
                .collect(toList());
    }
}
