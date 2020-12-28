import { Observable } from "rxjs";
import { IMessage } from "@stomp/stompjs"
import { SensorLatestService, SensorLatestServiceCompanion } from "../sensor-latest.service";
import { SensorMeasureLatestDto, SensorMeasureMetaData, SensorMeasureType } from "../../sensor.classes";
import { environment } from "src/environments/environment";
import { map } from "rxjs/operators";

export interface SimpleRxStompService {
  watch(destination: string): Observable<IMessage>;
}

export class SensorLatestWsServiceNative implements SensorLatestService {
    
  private readonly measureReceiveRootEndpoint: string = environment.wsStompSubscribePrefix + "/sensor/live";

  private _lastMeasuresByTypeByLocation: Map<string, Map<SensorMeasureType, SensorMeasureLatestDto>>
  private backendRxByLocation: Map<string, Observable<Map<SensorMeasureType, SensorMeasureLatestDto>>>;


  constructor(public ngRxStompService: SimpleRxStompService) {
      this._lastMeasuresByTypeByLocation = new Map();
      this.backendRxByLocation = new Map();
  }

  get lastMeasuresByTypeByLocation():  Map<string, Map<SensorMeasureType, SensorMeasureLatestDto>>{
      return this._lastMeasuresByTypeByLocation;
  }

  static filterLatestMeasuresAsync(measureByType$: Observable<Map<SensorMeasureType, SensorMeasureLatestDto>>, newSensorKeys: SensorMeasureMetaData[]): Observable<Map<SensorMeasureType, SensorMeasureLatestDto>> {
    const allowedSensorTypes: Set<SensorMeasureType> = new Set();
    newSensorKeys.forEach( sensorKey => allowedSensorTypes.add(sensorKey.type));

    return measureByType$
        .pipe(map(latestMeasuresByType => SensorLatestWsServiceNative.mapWithTargetMeasures(latestMeasuresByType, allowedSensorTypes)));
 }


  getLatestMeasuresAsync(newSensorKeys: SensorMeasureMetaData[], location: string): Observable<Map<SensorMeasureType, SensorMeasureLatestDto>> {
    const measureByType$ = this.getConnection(location);
    if (!newSensorKeys || newSensorKeys.length == 0) {
        return measureByType$;
    } else {
        return SensorLatestWsServiceNative.filterLatestMeasuresAsync(measureByType$, newSensorKeys)
    }
  }

  private static mapWithTargetMeasures(latestMeasuresByType : Map<SensorMeasureType, SensorMeasureLatestDto>, targetSensorTypes: Set<SensorMeasureType>) : Map<SensorMeasureType, SensorMeasureLatestDto>{
      const filteredMap = new Map()
      latestMeasuresByType.forEach( (value, key, _) => {
          if (targetSensorTypes.has(key)) {
              filteredMap.set(key, value);
          }
      })
      return filteredMap;
  }

  private getConnection(location: string) : Observable<Map<SensorMeasureType, SensorMeasureLatestDto>> {
      if (!this.backendRxByLocation.has(location)){
          const backendRxForLocation = this.ngRxStompService
          .watch(this.measureReceiveRootEndpoint + "/" + location)
          .pipe(map( message => {
              const latestMeasure = SensorMeasureLatestDto.deepCopy(JSON.parse(message.body))
              return this.updateAndCopyLatestMeasuresMap(latestMeasure);
          }))
          this.backendRxByLocation.set(location, backendRxForLocation);
      }
      return this.backendRxByLocation.get(location);
  }

  private updateAndCopyLatestMeasuresMap(latestMeasure : SensorMeasureLatestDto) : Map<SensorMeasureType, SensorMeasureLatestDto> {
      var lastMeasuresByType = this._lastMeasuresByTypeByLocation.get(latestMeasure.location);

      if(!lastMeasuresByType) {
          lastMeasuresByType = new Map();
      }
      lastMeasuresByType.set(latestMeasure.type, latestMeasure);

      this._lastMeasuresByTypeByLocation.set(latestMeasure.location, lastMeasuresByType)
      return SensorLatestServiceCompanion.copyMeasuresMap(lastMeasuresByType);
  }
}