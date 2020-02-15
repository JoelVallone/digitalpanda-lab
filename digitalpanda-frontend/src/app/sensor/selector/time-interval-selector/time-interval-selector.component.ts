import { Component, Input, OnInit, OnChanges, SimpleChanges, Output, EventEmitter, Injectable } from '@angular/core';
import { FormGroup, FormArray, FormControl, FormBuilder } from '@angular/forms';
import {NgbDate, NgbCalendar, NgbTimeStruct, NgbTimeAdapter} from '@ng-bootstrap/ng-bootstrap';

import { SensorMeasureMetaData, SensorMeasureTypeDetails, SensorMeasureType } from './../../sensor.classes';
import { map } from 'rxjs/operators';
import { SensorHistorySelectorFormService } from '../sensor-history-selector-form.service';

@Injectable()
export class NgbTimeStringAdapter extends NgbTimeAdapter<string> {

  fromModel(value: string): NgbTimeStruct {
    if (!value) {
      return null;
    }
    const split = value.split(':');
    return {
      hour: parseInt(split[0], 10),
      minute: parseInt(split[1], 10),
      second: parseInt(split[2], 10)
    };
  }

  toModel(time: NgbTimeStruct): string {
    if (!time) {
      return null;
    }
    return `${time.hour}:${time.minute}:${time.second}`;
  }
}


@Component({
  selector: 'app-time-interval-selector',
  templateUrl: './time-interval-selector.component.html',
  styleUrls: ['./time-interval-selector.component.scss'],
  providers: [{provide: NgbTimeAdapter, useClass: NgbTimeStringAdapter}]
})
export class TimeIntervalSelectorComponent {

  @Input() public timeIntervalForm: FormGroup;

  fromDate: NgbDate;
  toDate: NgbDate;
  displayDatePicker: boolean;

  hoveredDate: NgbDate;

  DEFAULT_RELATIVE_TIME = '24H';
  displayRelativeTimeSelector: boolean;
  selectedRelativeTime: string;
  relativeTimeKeys: Array<string> = new Array();
  relativeTimeMap: Map<string, number> = new Map();

  fromTime: NgbTimeStruct;
  toTime: NgbTimeStruct;

  constructor(calendar: NgbCalendar, public formService: SensorHistorySelectorFormService) {

    this.addRelativeTime('1H',       1 * 60 * 60 * 1000);
    this.addRelativeTime('4H',       4 * 60 * 60 * 1000);
    this.addRelativeTime('8H',       8 * 60 * 60 * 1000);
    this.addRelativeTime('12H',     12 * 60 * 60 * 1000);
    this.addRelativeTime('24H',     24 * 60 * 60 * 1000);
    // this.addRelativeTime('2D',  2 * 24 * 60 * 60 * 1000); // TODO: Add interval pre-computation for larger queries
    // this.addRelativeTime('7D',  7 * 24 * 60 * 60 * 1000);

    this.displayDatePicker = true;
    this.displayRelativeTimeSelector = true;
    this.setDefaultInterval();
  }


  setDefaultInterval() {
    this.selectRelativeTime(this.DEFAULT_RELATIVE_TIME);
  }
  private addRelativeTime(timeDisplayName: string, timeMillis: number) {
    this.relativeTimeMap.set(timeDisplayName, timeMillis);
    this.relativeTimeKeys.push(timeDisplayName);
  }

  selectRelativeTime(timeDisplayName: string) {
    this.selectedRelativeTime = timeDisplayName;
    this.formService.setRelativeIntervalFromNow(this.relativeTimeMap.get(timeDisplayName));
    this.fetchDatesFromFormService();
  }

  private toFormViewDate(date: Date): NgbDate {
    return new NgbDate(date.getFullYear(), date.getMonth() + 1, date.getDate());
  }

  private toStdDate(viewDate: NgbDate): Date {
    return viewDate ? new Date(viewDate.year, viewDate.month - 1, viewDate.day) :  null ;
  }

  private updateToDate(date: NgbDate): void {
    this.formService.updateToDate(this.toStdDate(date));
  }

  private updateFromDate(date: NgbDate): void {
    this.formService.updateFromDate(this.toStdDate(date));
  }

  private fetchDatesFromFormService(): void {
    this.fromDate = this.toFormViewDate(this.formService.getFromDateStd());
    this.toDate =  this.toFormViewDate(this.formService.getToDateStd());
  }

  public toggleIntervalSelector() {
    this.displayRelativeTimeSelector = !this.displayRelativeTimeSelector;
  }

  public toggleDatePicker() {
    this.fetchDatesFromFormService();
    if (!this.fromDate || !this.toDate) {
      this.setDefaultInterval();
    }
    this.displayDatePicker = !this.displayDatePicker;
  }

  // ================== datepicker view logic ====================
  onDateSelection(date: NgbDate) {
    if (!this.fromDate && !this.toDate) {
      this.fromDate = date;
    } else if (this.fromDate && !this.toDate && (date.after(this.fromDate) || date.equals(this.fromDate))) {
      this.toDate = date;
    } else {
      this.toDate = null;
      this.fromDate = date;
    }
    this.updateToDate(this.toDate);
    this.updateFromDate(this.fromDate);
  }

  isHovered(date: NgbDate) {
    return this.fromDate && !this.toDate && this.hoveredDate && date.after(this.fromDate) && date.before(this.hoveredDate);
  }

  isInside(date: NgbDate) {
    return date.after(this.fromDate) && date.before(this.toDate);
  }

  isRange(date: NgbDate) {
    return date.equals(this.fromDate) || date.equals(this.toDate) || this.isInside(date) || this.isHovered(date);
  }

}
