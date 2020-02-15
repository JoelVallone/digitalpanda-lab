import {Component, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {map, tap} from 'rxjs/operators';

import {SensorService} from './../sensor.service';
import {SensorMeasureMetaData} from './../sensor.classes';

@Component({
  selector: 'app-live-sensor',
  templateUrl: './live.sensor.component.html'
})
export class LiveSensorComponent implements OnInit {

  availableMeasureLocations: Set<string>;
  selectedMeasureLocations: Set<string>;
  measureKeysByLocation$: Observable<Map<string, Array<SensorMeasureMetaData>>>;

  private measureKeys$:  Observable<Array<SensorMeasureMetaData>>;

  test: Set<string>;

  constructor(public sensorService: SensorService) { }

  ngOnInit() {
    const that = this;
    this.selectedMeasureLocations = new Set<string>();
    this.availableMeasureLocations = new Set<string>();

    this.measureKeys$ = this.sensorService.loadMeasurekeys();

    this.measureKeys$.subscribe((measureKeysArray: Array<SensorMeasureMetaData>) => {
      const measureLocationSet = new Set(measureKeysArray.map(measureKey => measureKey.location));
      that.availableMeasureLocations = measureLocationSet;
    });

    this.measureKeysByLocation$ = this.measureKeys$
      .pipe(map( (measureKeys: Array<SensorMeasureMetaData>) => {
        const latestMeasureKeysByLocation = new Map<string, Array<SensorMeasureMetaData>>();
        measureKeys.forEach((measureKey) => this.putByLocation(latestMeasureKeysByLocation,  measureKey));
        return latestMeasureKeysByLocation;
      }));
  }

  onLocationSelection(newLocationSelection: Set<string>): void {
    this.selectedMeasureLocations = new Set(newLocationSelection);
  }

  private putByLocation(mapRef: Map<string, Array<SensorMeasureMetaData>>, measureKey: SensorMeasureMetaData): void {
      mapRef.has(measureKey.location) ? mapRef.get(measureKey.location).push(measureKey) : mapRef.set(measureKey.location, [measureKey]);
  }
}
