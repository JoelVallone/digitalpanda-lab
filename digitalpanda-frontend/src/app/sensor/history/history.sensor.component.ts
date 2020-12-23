import { Component} from '@angular/core';
import { Observable } from 'rxjs';
import { SensorBackendService } from '../service/sensor.backend.service';
import { SensorMeasuresHistoryDto } from './../sensor.classes';
import { SensorHistorySelection } from '../selector/sensor-history-selector-form.service';
import { SensorMeasureType } from '../sensor.classes';

@Component({
  selector: 'app-sensor-history',
  templateUrl: './history.sensor.component.html'
})
export class HistorySensorComponent {
  private readonly DATA_POINTS_PER_SAMPLE: number = 250;
  sensorsMeasures$: Array<Observable<Array<SensorMeasuresHistoryDto>>>;
  sensorsMeasures: Array<SensorMeasuresHistoryDto>;

  sensorHistorySelection: SensorHistorySelection;
  selectedMeasureTypes: Set<SensorMeasureType>;
  isSelectionCollapsed: boolean;
  isSelectionPristine: boolean;
  historySelectionLoadedCount: number;

  private testSelection = {
    fromMillis: 1545695940000,
    toMillis: 1545696600000,
    measureSelection: [
      {
        location: 'panda-home',
        type: SensorMeasureType.TEMPERATURE
      },
      {
        location: 'panda-home',
        type: SensorMeasureType.HUMIDITY
      },
      {
        location: 'panda-outdoor',
        type: SensorMeasureType.TEMPERATURE
      }
    ]
  };


  constructor(public sensorService: SensorBackendService) {
    this.historySelectionLoadedCount = 0;
    this.isSelectionCollapsed = true;
    this.isSelectionPristine = true;
    this.sensorHistorySelection = new SensorHistorySelection(0, 0, []);
    this.selectedMeasureTypes = new Set();
    this.sensorsMeasures$ = [];
    this.sensorsMeasures = [];
  }

  toggleSensorSelection() {
    this.isSelectionCollapsed = !this.isSelectionCollapsed;
  }

  getSensorSelectionText(): string {
    return this.isSelectionCollapsed ? (this.isSelectionPristine ? 'Select data' : 'Change data selection') : 'Hide data selection' ;
  }

  onSensorHistorySelection(newSensorHistorySelection: SensorHistorySelection) {
    this.isSelectionCollapsed = true;
    this.isSelectionPristine = false;
    this.historySelectionLoadedCount = 0;
    this.updateSensorSelection(newSensorHistorySelection);
    this.sensorsMeasures.splice(0);
    this.sensorsMeasures$ = this.batchLoadSensorHistory(newSensorHistorySelection);
    this.sensorsMeasures$.forEach(sensorMeasures$ =>
      sensorMeasures$.subscribe((newSensorsMeasures) => {
        newSensorsMeasures.forEach((sensorMeasures) => this.sensorsMeasures.push(sensorMeasures));
        this.historySelectionLoadedCount++;
      }));
  }

  isLoading(): boolean {
    return  this.isSelectionCollapsed && !this.isSelectionPristine && !this.canDisplayData();
  }
  canDisplayData(): boolean {
    return this.sensorHistorySelection && this.historySelectionLoadedCount === this.sensorHistorySelection.measureSelection.length
            && this.sensorHistorySelection.measureSelection.length !== 0;
  }

  private updateSensorSelection(newSensorHistorySelection: SensorHistorySelection) {
    this.sensorHistorySelection = newSensorHistorySelection;
    this.selectedMeasureTypes.clear();
    this.sensorHistorySelection.measureSelection
      .forEach((selectedMeasure) => this.selectedMeasureTypes.add(selectedMeasure.type));
  }

  private batchLoadSensorHistory(newSensorHistorySelection: SensorHistorySelection): Array<Observable<Array<SensorMeasuresHistoryDto>>> {
    return newSensorHistorySelection.measureSelection
                  .map(selectedMeasure =>
                      this.sensorService.loadHistoryMeasures(
                        selectedMeasure,
                        newSensorHistorySelection.fromMillis,
                        newSensorHistorySelection.toMillis,
                        this.DATA_POINTS_PER_SAMPLE)
                      );
  }
}
