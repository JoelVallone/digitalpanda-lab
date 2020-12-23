import { Component, Input, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { SensorMeasureLatestDto, SensorMeasureMetaData, SensorMeasureType, SensorMeasureTypeDetails } from './../../sensor.classes';
import { SensorBackendService } from '../../service/sensor.backend.service';
import { first, map } from 'rxjs/operators';
import { SensorLatestService } from '../../service/sensor-latest.service';


export class ViewMeasure {
  constructor(
    public measureTypeDetails?: SensorMeasureTypeDetails,
    public sensorKey?: SensorMeasureMetaData,
    public measure?: SensorMeasureLatestDto,
  ) {}
}

@Component({
  selector: 'app-location-values-latest',
  templateUrl: './location-values-display.latest.component.html',
  styleUrls: ['./location-values-display.latest.component.scss']
})
export class LocationValuesDisplayLatestComponent implements OnChanges {

  @Input() public location: string;
  @Input() public sensorKeys: Array<SensorMeasureMetaData>;

  public viewMeasures$: Observable<Array<ViewMeasure>>;
  private sensorLatestService: SensorLatestService;

  constructor(public sensorBackendService: SensorBackendService) {
    this.sensorLatestService = new SensorLatestService(sensorBackendService);
    this.viewMeasures$ = this.sensorLatestService
    .getLatestMeasuresAsync()
    .pipe(
      map( measureByType =>  [ ...measureByType.values()].map(m => this.asViewMeasure(m)))
    );
  }

  private asViewMeasure(measureDto: SensorMeasureLatestDto): ViewMeasure{
    return new ViewMeasure(
      SensorMeasureMetaData.getTypeDetail(measureDto.type),
      new SensorMeasureMetaData(measureDto.location, measureDto.type),
      measureDto);
  }

  ngOnChanges(changes: SimpleChanges) {
    if ( this.isSetAndhasChanged(changes, 'location') || this.isSetAndhasChanged(changes, 'sensorKeys')) {
      this.sensorLatestService.updateTargetMeasures(
        changes.sensorKeys.currentValue as Array<SensorMeasureMetaData>,
        changes.location.currentValue as string);
    }
  }

  private isSetAndhasChanged(changes: SimpleChanges, key: string): boolean {
      return changes[key]
      && changes[key].currentValue
      && JSON.stringify(changes[key].previousValue) !== JSON.stringify(changes[key].currentValue);
  }
}
