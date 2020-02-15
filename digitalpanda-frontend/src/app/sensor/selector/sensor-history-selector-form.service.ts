import { Injectable } from '@angular/core';
import { FormGroup, FormArray, FormControl, FormBuilder, AbstractControl, ValidationErrors, Validators } from '@angular/forms';
import { Observable } from 'rxjs';

import { SensorService } from '../sensor.service';
import { SensorMeasureMetaData, SensorMeasureTypeDetails, SensorMeasureType } from './../sensor.classes';

export class SensorHistorySelection {
  constructor(public fromMillis: number, public toMillis: number, public measureSelection: Array<SensorMeasureMetaData>) {}
}

@Injectable()
export class SensorHistorySelectorFormService {

  public static DEFAULT_INTERVAL_LENGTH_MILLIS: number = 24 * 3600 * 1000;
  public MAX_TIME_INTERVAL_MILLIS: number = 1 * 24 * 3600 * 1000;

  public form: FormGroup;
  private _measureTypesByLocationMap = new Map<string, Array<SensorMeasureMetaData>>();

  constructor(public sensorService: SensorService, private fb: FormBuilder) {
    const intervalTo: Date = new Date();
    const intervalFrom: Date = new Date(intervalTo.getTime() - SensorHistorySelectorFormService.DEFAULT_INTERVAL_LENGTH_MILLIS);
    this.form  = this.fb.group({
      sensorsIdentification : this.fb.group({
        locations : this.fb.array([]),
        locationsMeasures: this.fb.array([], (fa) => this.atLeastOneMeasureSelectedValidator(fa))}),
      timeInterval: this.fb.group({
          from: this.fb.group({
            date: this.fb.control(this.toStringDate(intervalFrom), [Validators.required, (fc) => this.rawDateFormatValidator(fc)]),
            time: this.fb.control(this.toStringTime(intervalFrom), Validators.required),
          }),
          to: this.fb.group({
            date: this.fb.control(this.toStringDate(intervalTo), [Validators.required, (fc) => this.rawDateFormatValidator(fc)]),
            time: this.fb.control(this.toStringTime(intervalTo), Validators.required),
          })
        },
        {
          validator:  (fg) => this.timeIntervalValidator(fg)
        })
    });
    this.subscribeInternalListeners();
  }

  public setRelativeIntervalFromNow(deltaMillis: number): void {
    const intervalTo: Date = new Date();
    this.updateDate('to', intervalTo);
    this.updateTime('to', intervalTo);

    const intervalFrom: Date = new Date(intervalTo.getTime() - deltaMillis);
    this.updateDate('from', intervalFrom);
    this.updateTime('from', intervalFrom);
  }

  public toStringTime(time: Date): string {
    return time.getHours() + ':' + time.getMinutes() + ':' + time.getSeconds();
  }

  public parseTime(timeStr: string): Date {
    if (!timeStr) { return null; }
    const timeArr: string[] = timeStr.split(':');
    if (timeArr.length !== 3) { return null; }
    const time: Date = new Date();
    time.setHours(+timeArr[0]);
    time.setMinutes(+timeArr[1]);
    time.setSeconds(+timeArr[2]);
    return time;
  }

  private subscribeInternalListeners(): void {
    this.locations.valueChanges.subscribe(locationsVal => {
      const selectedLocations: Array<string> = locationsVal
          .filter(location => location.isSelected)
          .map( location => location.location);
      this.setLocationMeasures(this.getMeasureTypesByLocation(selectedLocations));
    });
  }

  get locationsMeasures(): FormArray {
    return this.form.get('sensorsIdentification').get('locationsMeasures') as FormArray;
  }

  get locations(): FormArray {
    return this.form.get('sensorsIdentification').get('locations') as FormArray;
  }

  get timeInterval(): FormGroup {
    return this.form.get('timeInterval')as FormGroup;
  }

  private getDate(toOrFrom: string): string {
    return this.timeInterval.get(toOrFrom).get('date').value;
  }

  public getToDateStd(): Date {
    return this.parseDate(this.getDate('to'));
  }

  public getFromDateStd(): Date {
    return this.parseDate(this.getDate('from'));
  }

  public timeIntervalValidator(timeIntervalControl: AbstractControl): ValidationErrors {
    const fromMillis = this.getTimestampMillis('from', timeIntervalControl);
    const toMillis = this.getTimestampMillis('to', timeIntervalControl);
    if (fromMillis && toMillis) {
      if (toMillis < (fromMillis + 1000)) {
        return {timestampPrecedence: true};
      } else if (toMillis > (fromMillis + this.MAX_TIME_INTERVAL_MILLIS + 1000)) {
        return {intervalOverflow: true};
      }
    }
    return null;
  }

  public getTimestampMillis(fromOrTo: string, timeIntervalControl?: AbstractControl): number {
    if (!timeIntervalControl) {
      timeIntervalControl = this.timeInterval;
    }

    const dateCg = timeIntervalControl.get(fromOrTo).get('date');
    const date: Date = this.parseDate(dateCg.value);
    if (!date) { return null; }

    const timeCg = timeIntervalControl.get(fromOrTo).get('time');
    const time: Date = this.parseTime(timeCg.value);
    if (!time) { return null; }

    date.setHours(time.getHours());
    date.setMinutes(time.getMinutes());
    date.setSeconds(time.getSeconds());

    return date.getTime();
  }

  public rawDateFormatValidator(control: AbstractControl): ValidationErrors {
    if (!control.value || !this.isRawDateValid(control.value)) {
      return {rawDateFormat: true};
    }

    return null;
  }

  public isRawDateValid(date: string): Boolean {
    if (date && date.match(/^[0-9]{1,4}-[0-9]{1,2}-[0-9]{1,2}$/)) {
      const dateElements: Array<string> =  date.split('-');
      const month: number = +dateElements[1];
      const day: number = +dateElements[2];
      return (month >= 1 && month <= 12 && day >= 1 && day <= 31);
    }
    return false;
  }

  // Expected format : yyyy-mm-dd
  private parseDate(date: string): Date {
    if (!this.isRawDateValid(date)) { return null; }
    const dateElements: Array<string> =  date.split('-');
    return new Date(+dateElements[0], +dateElements[1] - 1, +dateElements[2]);
  }

  private toStringDate(date: Date): string {
    const dateString: string = date.getFullYear() + '-'  + (date.getMonth() + 1) + '-' + date.getDate();
    return dateString;
  }

  public updateToDate(date: Date): void {
    this.updateDate('to', date);
  }

  public updateFromDate(date: Date): void {
    this.updateDate('from', date);
  }

  private updateDate(toOrFrom: string, date: Date): void {
    const dateFc: FormControl = this.timeInterval.get(toOrFrom).get('date') as FormControl;
    if (date) {
      dateFc.setValue(this.toStringDate(date));
    } else {
      dateFc.reset();
    }
  }

  public updateToTime(date: Date): void {
    this.updateTime('to', date);
  }

  public updateFromTime(date: Date): void {
    this.updateTime('from', date);
  }

  private updateTime(toOrFrom: string, date: Date): void {
    const dateFc: FormControl = this.timeInterval.get(toOrFrom).get('time') as FormControl;
    if (date) {
      dateFc.setValue(this.toStringTime(date));
    } else {
      dateFc.reset();
    }
  }

  public updateFormData(newMeasureKeys: Array<SensorMeasureMetaData>): FormGroup {
    this.updateLocalSensorCache(newMeasureKeys);
    this.updateSelectableLocations(this.getAllStoredLocations(), false);
    return this.form;
  }

  private updateLocalSensorCache(newMeasureKeys: Array<SensorMeasureMetaData>) {
    this._measureTypesByLocationMap = new Map<string, Array<SensorMeasureMetaData>>();
    newMeasureKeys.forEach(measureKey => {
      if (this._measureTypesByLocationMap.has(measureKey.location)) {
        this._measureTypesByLocationMap.get(measureKey.location).push(measureKey);
      } else {
        this._measureTypesByLocationMap.set(measureKey.location, [measureKey]);
      }
    });
  }

  private getAllStoredLocations(): Array<string> {
    const newLocations: Array<string> = [];
    this._measureTypesByLocationMap.forEach( (_, location: string) => newLocations.push(location));
    return newLocations;
  }

  private getMeasureTypesByLocation(locations: Array<string> ): Map<string, Array<SensorMeasureMetaData>> {
    const selectedMeasuresByLocation = new Map<string, Array<SensorMeasureMetaData>>();
    locations.forEach(location =>
      selectedMeasuresByLocation.set(location, this._measureTypesByLocationMap.get(location)));
    return selectedMeasuresByLocation;
  }

  private updateSelectableLocations(newLocations: Array<String>, initialSelectionValue: Boolean): void {
    this.locations.controls.splice(0);
    newLocations.forEach(( location: string) => {
        this._measureTypesByLocationMap.get(location);
        const locationGroup = this.fb.group({
          location,
          isSelected: this.fb.control(initialSelectionValue)
        });
        this.locations.push(locationGroup);
      }
    );
  }

  private setLocationMeasures(measureTypesByLocationMap: Map<string, Array<SensorMeasureMetaData>>): void {
    const measureTypeNameSelectionByLocation:  Map<string, Set<string>> = this.extractMeasureTypeNameSelectionByLocation();
    this.clearLocationsMeasures();
    measureTypesByLocationMap.forEach(
      (measureTypes: SensorMeasureMetaData[], location: string) => {
        const locationMeasures: FormGroup =  this.fb.group({
            location,
            measures : this.fb.array([])
          });

        measureTypes.forEach((measureKey) => {
          const measureTypeName = SensorMeasureMetaData.getTypeDetail(measureKey.type).typeName;
          const previouslySelected: boolean = measureTypeNameSelectionByLocation.get(location) ?
          measureTypeNameSelectionByLocation.get(location).has(measureTypeName) : false;
          (locationMeasures.get('measures') as FormArray).push(
            this.fb.group({
              measureKey,
              measureTypeName,
              isSelected: this.fb.control(false || previouslySelected)
            }));
        });

        this.locationsMeasures.push(locationMeasures);
    });
  }

  public isMeasureTypesByLocationEmtpy(): boolean {
    return this.locationsMeasures.length === 0;
  }

  public isMeasureTypesByLocationFull(): boolean {
    return this.locationsMeasures.length === this.locations.length && this.locations.length !== 0;
  }

  public selectAllLocations(): void {
    this.updateSelectableLocations(this.getAllStoredLocations(), true);
  }

  public clearAllLocations(): void {
    this.updateSelectableLocations(this.getAllStoredLocations(), false);
    this.clearLocationsMeasures();
  }

  public isMeasureTypesForLocationEmpty(location: string): boolean {
    return this.isMeasureTypesForLocationAllSame(location, false);
  }

  public isMeasureTypesForLocationFull(location: string): boolean {
    return this.isMeasureTypesForLocationAllSame(location, true);
  }

  public isMeasureTypesForLocationAllSame(location: string, isSelected: boolean): boolean {
    return this.locationsMeasures.controls
    .filter((locationMeasures) => location === locationMeasures.get('location').value )
    .every(locationMeasures =>
        (locationMeasures.get('measures') as FormArray).controls
            .every(measure => measure.get('isSelected').value === isSelected));
  }

  public selectAllForLocation(location: string): void {
    this.setAllForLocation(location, true);
  }

  public clearAllForLocation(location: string): void {
    this.setAllForLocation(location, false);
  }

  public setAllForLocation(location: string, isSelected: boolean): void {
    this.locationsMeasures.controls
    .filter((locationMeasures) => location === locationMeasures.get('location').value )
    .forEach(locationMeasures =>
        (locationMeasures.get('measures') as FormArray).controls
            .forEach(measure => measure.get('isSelected').setValue(isSelected)));
  }

  public clearLocationsMeasures(): void {
    while (this.locationsMeasures.length !== 0) {
      this.locationsMeasures.removeAt(0);
    }
  }

  public extractMeasureTypeNameSelectionByLocation(): Map<string, Set<string>> {
    const locationsMeasuresSelection = new Map<string, Set<string>>();
    (this.locationsMeasures as FormArray).controls.forEach(locationMeasures => {
        const locationMeasuresSelection = new Set<string>();
        (locationMeasures.get('measures') as FormArray).controls.forEach(measure => {
          if (measure.get('isSelected').value) {
            locationMeasuresSelection.add(measure.get('measureTypeName').value);
          }
        });
        locationsMeasuresSelection.set(locationMeasures.get('location').value, locationMeasuresSelection);
    });

    return locationsMeasuresSelection;
  }

  public extractMeasureSelectionList(): Array<SensorMeasureMetaData> {
    const measureKeysSelection: Array<SensorMeasureMetaData> = [];
    this.locationsMeasures.controls.forEach(locationMeasures => {
      (locationMeasures.get('measures') as FormArray).controls.forEach(measure => {
        if (measure.get('isSelected').value) {
          measureKeysSelection.push(measure.get('measureKey').value);
        }
      });
    });
    return measureKeysSelection;
  }

  private atLeastOneMeasureSelectedValidator(locationsMeasures: AbstractControl): ValidationErrors {
    const atLeastOneSelected: Boolean = (locationsMeasures as FormArray).controls.length !== 0
    && (locationsMeasures as FormArray).controls
        .some(locationMeasures =>
            (locationMeasures.get('measures') as FormArray).controls
                .some(measure => measure.get('isSelected').value));
    return atLeastOneSelected ? null : {atLeastOneMeasureSelected : true};
  }

  public canSubmit(): Boolean {
    return this.form.valid;
  }

  public extractSubmission(): SensorHistorySelection {
    if (!this.canSubmit()) {
      console.error('Invalid form' + JSON.stringify(this.form.errors));
      return;
    }
    return new SensorHistorySelection(
                this.getTimestampMillis('from'),
                this.getTimestampMillis('to'),
                this.extractMeasureSelectionList());
  }
}
