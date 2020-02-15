
/* These are JavaScript import statements. Angular doesn’t know anything about these. */
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { NgxChartsModule } from '@swimlane/ngx-charts';

import { UiModule } from '../ui/ui.module';
import { LocationValuesDisplayLatestComponent } from './live/location-values-display//location-values-display.latest.component';
import { DebugDisplayHistoryComponent } from './history/debug-display/debug-display.history.component';
import { LiveSensorComponent } from './live/live.sensor.component';
import { HistorySensorComponent } from './history/history.sensor.component';
import { SensorHistorySelectorComponent } from './selector/sensor-history-selector.component';
import { SensorLocationSelectorComponent } from './selector/sensor-location-selector/sensor-location-selector.component';
import { SensorHistorySelectorFormService } from './selector/sensor-history-selector-form.service';
import { SensorService } from './sensor.service';
import { SensorIdentificationComponent } from './selector/sensor-identification/sensor-identification.component';
import { TimeIntervalSelectorComponent } from './selector/time-interval-selector/time-interval-selector.component';
import { MultiChartHistoryComponent } from './history/multi-chart/multi-chart.history.component';

@NgModule({
  imports: [
    CommonModule,  /* These are NgModule imports used by Angular. */
    ReactiveFormsModule,
    FormsModule,
    BrowserModule,
    BrowserAnimationsModule,
    NgbModule,
    UiModule,
    NgxChartsModule
  ],
  providers: [
    SensorService, SensorHistorySelectorFormService
  ],
  declarations: [
    LocationValuesDisplayLatestComponent, SensorLocationSelectorComponent,
    DebugDisplayHistoryComponent, MultiChartHistoryComponent, LiveSensorComponent, HistorySensorComponent,
    SensorIdentificationComponent, TimeIntervalSelectorComponent, SensorHistorySelectorComponent,
  ],
  exports: [
    LiveSensorComponent, HistorySensorComponent
  ]
})
export class SensorModule { }
