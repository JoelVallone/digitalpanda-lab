import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import { Observable } from 'rxjs';
import { SensorMeasureLatestDto, SensorMeasureMetaData, SensorMeasureType, SensorMeasureTypeDetails } from './../../sensor.classes';
import { SensorBackendService } from '../../service/sensor.backend.service';
import { map } from 'rxjs/operators';
import { SensorLatestHttpPollingService } from '../../service/sensor-latest-http-polling.service';
import { SensorLatestService } from '../../service/sensor-latest.service';
import { SensorLatestWsService } from '../../service/ws/sensor-latest-ws.service';
import { environment } from 'src/environments/environment';


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
export class LocationValuesDisplayLatestComponent implements OnInit {

  @Input() public location: string;
  @Input() public sensorKeys: Array<SensorMeasureMetaData>;

  public viewMeasures$: Observable<Array<ViewMeasure>>;
  public viewMeasures: Array<ViewMeasure>;
  private sensorLatestService: SensorLatestService;

  constructor(public sensorBackendService: SensorBackendService, sensorLatestWsService: SensorLatestWsService) {
    if (environment.enableWebsocket && window.WebSocket) {
      this.sensorLatestService = sensorLatestWsService;
    } else {
      this.sensorLatestService = new SensorLatestHttpPollingService(sensorBackendService);
    }
    this.viewMeasures = new Array();
  }

  ngOnInit(): void {
    this.viewMeasures$ = this.sensorLatestService
      .getLatestMeasuresAsync(this.sensorKeys, this.location)
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
}
