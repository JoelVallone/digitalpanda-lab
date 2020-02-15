import { Component, Input, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { SensorMeasureLatestDto, SensorMeasureMetaData, SensorMeasureType, SensorMeasureTypeDetails } from './../../sensor.classes';
import { SensorService } from './../../sensor.service';


export class ViewMeasure {
  constructor(
    public measureTypeDetails?: SensorMeasureTypeDetails,
    public sensorKey?: SensorMeasureMetaData,
    public isLoaded?: boolean,
    public measure?: SensorMeasureLatestDto,
  ) {}
}

@Component({
  selector: 'app-location-values-latest',
  templateUrl: './location-values-display.latest.component.html',
  styleUrls: ['./location-values-display.latest.component.scss']
})
export class LocationValuesDisplayLatestComponent implements OnChanges, OnDestroy {

  @Input() public location: string;
  @Input() public sensorKeys: Array<SensorMeasureMetaData>;

  public viewMeasures: Array<ViewMeasure>;
  public periodicServiceCallHandle?: any;

  constructor(public sensorService: SensorService) {
    this.viewMeasures = [];
  }

  ngOnChanges(changes: SimpleChanges) {
    if ( this.isSetAndhasChanged(changes, 'location') || this.isSetAndhasChanged(changes, 'sensorKeys')) {
      this.cancelDataAutoRefresh();
      this.startDataAutoRefresh(
        changes.sensorKeys.currentValue as Array<SensorMeasureMetaData>,
        changes.location.currentValue as string
      );
    }
  }

  private isSetAndhasChanged(changes: SimpleChanges, key: string): boolean {
      return changes[key]
      && changes[key].currentValue
      && JSON.stringify(changes[key].previousValue) !== JSON.stringify(changes[key].currentValue);
  }

  ngOnDestroy() {
    this.cancelDataAutoRefresh();
  }

  private cancelDataAutoRefresh(): void {
    clearInterval(this.periodicServiceCallHandle);
  }

  private startDataAutoRefresh(sensorKeys:  Array<SensorMeasureMetaData>, location: string): void {
    const that = this;
    this.viewMeasures = [];
    sensorKeys
      .filter((sensorKey) => sensorKey.location === location)
      .forEach((sensorKey) => {
          const viewMeasure: ViewMeasure = new ViewMeasure();
          viewMeasure.isLoaded = false;
          viewMeasure.measureTypeDetails = SensorMeasureMetaData.getTypeDetail(sensorKey.type);
          viewMeasure.sensorKey = sensorKey;
          that.viewMeasures.push(viewMeasure);
      });
      that.loadAndSetMeasureCallback(that, this.viewMeasures);
      this.periodicServiceCallHandle =  setInterval((ref) => {that.loadAndSetMeasureCallback(that, this.viewMeasures); }, 1000);
  }

  private loadAndSetMeasureCallback(that, viewMeasures: Array<ViewMeasure>): void {
    viewMeasures.forEach((viewMeasure) =>
      that.sensorService.loadLatestMeasure(viewMeasure.sensorKey)
        .subscribe((latestMeasure) => {
          viewMeasure.measure = latestMeasure;
          viewMeasure.isLoaded = true;
        }));
  }
}
