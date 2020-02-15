import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { NgbAccordion } from '@ng-bootstrap/ng-bootstrap';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { SensorService } from './../sensor.service';
import { SensorMeasureMetaData } from './../sensor.classes';
import { SensorHistorySelectorFormService, SensorHistorySelection } from './sensor-history-selector-form.service';
import { FormGroup, FormsModule } from '@angular/forms';

@Component({
  selector: 'app-sensor-history-selector',
  templateUrl: './sensor-history-selector.component.html',
  styleUrls: ['./sensor-history-selector.component.scss']
})
export class SensorHistorySelectorComponent {

  @Output() public sensorHistorySelection =  new EventEmitter<SensorHistorySelection>();

  private measureTypesByLocationMap = new Map<string, Array<SensorMeasureMetaData>>();
  private _sensorForm: FormGroup;

  constructor(public formService: SensorHistorySelectorFormService, public sensorService: SensorService) {
    this._sensorForm = this.formService.form;
    this.sensorService
      .loadMeasurekeys()
      .subscribe((backendMeasureKeys) => this.formService.updateFormData(backendMeasureKeys));
  }


  get sensorForm(): FormGroup {
    return this._sensorForm;
  }

  canSubmit(): Boolean {
    return this.formService.canSubmit();
  }

  onSubmit() {
    this.sensorHistorySelection.emit(this.formService.extractSubmission());
  }
}
