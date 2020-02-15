import { Component, OnInit, Input, OnChanges, SimpleChanges } from '@angular/core';
import { TimeSeries, MultiChartHistoryService } from './multi-chart.history.service';
import { SensorMeasuresHistoryDto, SensorMeasureType, SensorMeasureMetaData } from '../../sensor.classes';


@Component({
  selector: 'app-multi-chart-history',
  templateUrl: './multi-chart.history.component.html',
  styleUrls: ['./multi-chart.history.component.scss']
})
export class MultiChartHistoryComponent implements OnChanges {

  @Input()  sensorsMeasures: Array<SensorMeasuresHistoryDto>;

  @Input()  fromMillis: number;
  @Input()  toMillis: number;

  @Input()  measureTypes: Set<SensorMeasureType>;

  private timeSeriesByType: Map<SensorMeasureType, Array<TimeSeries>>;

  // options
  xAxisLabel = 'Time';
  showXAxis = true;
  showYAxis = true;
  showLegend = true;
  showXAxisLabel = true;
  showYAxisLabel = true;
  timeline = true;
  autoScale = true;

  colorScheme = { domain: ['#5AA454', '#A10A28', '#C7B42C', '#AAAAAA'] };

  constructor() {
    this.timeSeriesByType = new Map();
  }

  getTimeSeriesForMeasureType(measureType: SensorMeasureType): Array<TimeSeries> {
    return this.timeSeriesByType.has(measureType) ? this.timeSeriesByType.get(measureType) : [];
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes && changes.sensorsMeasures.currentValue) {
        this.updateTimeSeries(changes.sensorsMeasures.currentValue);
    }
  }

  private updateTimeSeries(newSensorsMeasures: Array<SensorMeasuresHistoryDto>): void {
    this.timeSeriesByType = MultiChartHistoryService.toTimeSeriesByMeasureType(newSensorsMeasures);
  }

  getMeasureTypeFormatted(measureType: SensorMeasureType): string {
    return SensorMeasureMetaData.getTypeDetail(measureType).toString();
  }
}
