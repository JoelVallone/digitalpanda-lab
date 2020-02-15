import { Injectable } from '@angular/core';

import { SensorMeasuresHistoryDto, SensorMeasureType } from '../../sensor.classes';

export class TimeSeries {
 constructor(public name: string, public series: Array<TimedDataPoint>) {}
}

export class TimedDataPoint {
  constructor(public name: Date, public value: number) {}
}

@Injectable()
export class MultiChartHistoryService {

  constructor() {}

  static toTimeSeriesByMeasureType(sensorsMeasures: Array<SensorMeasuresHistoryDto>): Map<SensorMeasureType, Array<TimeSeries>> {
    const timeSeriesByMeasureType = new Map<SensorMeasureType, Array<TimeSeries>>();

    if (!sensorsMeasures) { return timeSeriesByMeasureType; }

    sensorsMeasures.forEach(sensorMeasures => {
      const timeSeries: TimeSeries = new TimeSeries(sensorMeasures.location, []);
      const offsetMillis = sensorMeasures.timeMillisBetweenDataPoints / 2;
      timeSeries.series = sensorMeasures.values
        .map((value, i) => new TimedDataPoint(
          new Date(sensorMeasures.startTimeMillisIncl + i * sensorMeasures.timeMillisBetweenDataPoints + offsetMillis),
          value)
        );
        MultiChartHistoryService.addToMap(timeSeriesByMeasureType, sensorMeasures.type , timeSeries);
    });
    return timeSeriesByMeasureType;
  }

  private static addToMap(map: Map<SensorMeasureType, Array<TimeSeries>>, key: SensorMeasureType, value: TimeSeries): void {
    map.has(key) ? map.get(key).push(value) : map.set(key, [value]);
  }
}
