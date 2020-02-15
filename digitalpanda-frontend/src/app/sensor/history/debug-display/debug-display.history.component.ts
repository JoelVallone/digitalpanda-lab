import { Component, OnChanges, Input, SimpleChanges } from '@angular/core';
import { SensorService } from './../../sensor.service';
import { SensorMeasuresHistoryDto, SensorMeasureMetaData, SensorMeasureType,
          SensorMeasureTypeDetails, SensorMeasureMean} from './../../sensor.classes';
import { SensorHistorySelection } from './../../selector/sensor-history-selector-form.service';
@Component({
  selector: 'app-debug-display-history',
  templateUrl: './debug-display.history.component.html',
  styleUrls: ['./debug-display.history.component.scss']
})
export class DebugDisplayHistoryComponent implements OnChanges {

  @Input() sensorHistorySelection: SensorHistorySelection;

  public measureLoaded: boolean;
  public sensor: SensorMeasureMetaData;
  public measureTypeDetails: SensorMeasureTypeDetails;
  public startTimeMillisIncl: number;
  public endTimeMillisExcl: number;
  public dataPointCount: number;

  public measuresIntervals: Array<Array<SensorMeasureMean>>;

  constructor(public sensorService: SensorService) {
    this.measureLoaded = false;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.sensorHistorySelection && changes.sensorHistorySelection.currentValue) {
      const current =  changes.sensorHistorySelection.currentValue;
      const previous =  changes.sensorHistorySelection.previousValue;
      console.log('previous : ' + JSON.stringify(previous));
      console.log('current : ' + JSON.stringify(current));
      if ( JSON.stringify(current) === JSON.stringify(previous)) {
          return;
      }
      this.updateDisplayData(changes.sensorHistorySelection.currentValue, 500);
      this.loadAndSetMeasureCallback(this, changes.sensorHistorySelection.currentValue, 500);
    }
  }

  private updateDisplayData(sensorHistorySelection: SensorHistorySelection, dataPointCount: number) {
    this.sensor = sensorHistorySelection.measureSelection[0];
    this.measureTypeDetails = SensorMeasureMetaData.getTypeDetail(this.sensor.type);
    this.startTimeMillisIncl = sensorHistorySelection.fromMillis;
    this.endTimeMillisExcl =   sensorHistorySelection.toMillis;
    this.dataPointCount = dataPointCount;
  }

  loadAndSetMeasureCallback(
    that: DebugDisplayHistoryComponent, sensorHistorySelection: SensorHistorySelection, dataPointCount: number): void {


    that.sensorService.loadHistoryMeasures(
      sensorHistorySelection.measureSelection[0],
      sensorHistorySelection.fromMillis,
      sensorHistorySelection.toMillis,
      dataPointCount)
    .subscribe((measuresIntervalsDto) => {
      that.measuresIntervals = new Array(measuresIntervalsDto.length);
      for (let i = 0; i < measuresIntervalsDto.length; i++) {
        const intervalDto =  measuresIntervalsDto[i];
        const measureInterval: Array<SensorMeasureMean> = new Array(intervalDto.values.length);
        const period = intervalDto.timeMillisBetweenDataPoints;
        const itervalStart = intervalDto.startTimeMillisIncl;
        measureInterval[0] = new SensorMeasureMean(intervalDto.values[0], itervalStart + (period / 2), itervalStart, itervalStart + period);
        for (let j = 1; j < intervalDto.values.length; j++) {
          measureInterval[j] = new SensorMeasureMean(
            intervalDto.values[j],
            itervalStart + (j + 0.5) * period,
            itervalStart + j * period,
            itervalStart + (j + 1.0) * period);
          }
        that.measuresIntervals[i] = measureInterval;
      }
      that.measureLoaded = true;
    });
  }
}
