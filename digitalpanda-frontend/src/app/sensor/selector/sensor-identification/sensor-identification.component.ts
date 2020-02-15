import { Component, Input, OnInit, OnChanges, SimpleChanges, Output, EventEmitter } from '@angular/core';
import { FormGroup, FormArray, FormControl, FormBuilder } from '@angular/forms';

import { SensorMeasureMetaData, SensorMeasureTypeDetails, SensorMeasureType } from './../../sensor.classes';
import { map } from 'rxjs/operators';
import { SensorHistorySelectorFormService } from '../sensor-history-selector-form.service';

@Component({
  selector: 'app-sensor-identification',
  templateUrl: './sensor-identification.component.html',
  styleUrls: ['./sensor-identification.component.scss']
})
export class SensorIdentificationComponent {
  @Input() public sensorsIdentificationForm: FormGroup;

  constructor(public formService: SensorHistorySelectorFormService) {}

  public isMeasureTypesByLocationEmtpy(): boolean {
    return this.formService.isMeasureTypesByLocationEmtpy();
  }

  public isMeasureTypesByLocationFull(): boolean {
    return this.formService.isMeasureTypesByLocationFull();
  }

  public selectAllLocations(): void {
    this.formService.selectAllLocations();
  }

  public clearAllLocations(): void {
    this.formService.clearAllLocations();
  }

  public isMeasureTypesForLocationEmpty(location: string): boolean {
    return this.formService.isMeasureTypesForLocationEmpty(location);
  }

  public isMeasureTypesForLocationFull(location: string): boolean {
    return this.formService.isMeasureTypesForLocationFull(location);
  }

  public selectAllForLocation(location: string): void {
    this.formService.selectAllForLocation(location);
  }

  public clearAllForLocation(location: string): void {
    this.formService.clearAllForLocation(location);
  }

  asFormArray(val: any): FormArray {
    return val;
  }

}
