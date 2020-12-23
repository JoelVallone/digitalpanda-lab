import { Injectable } from "@angular/core";
import { RxStompService } from "@stomp/ng2-stompjs";
import { Message } from '@stomp/stompjs';
import { Observable } from "rxjs";
import { map } from "rxjs/operators";
import { environment } from "src/environments/environment";
import { SensorMeasureMetaData, SensorMeasureType, SensorMeasureLatestDto } from "../sensor.classes";
import { SensorLatestService, SensorLatestServiceCompanion } from "./sensor-latest.service";

@Injectable({
    providedIn: 'root'
  })
export class SensorLatestWsService implements SensorLatestService {
    
    private readonly measureReceiveRootEndpoint: string = environment.wsStompSubscribePrefix + "/sensor/live";

    private lastMeasuresByTypeByLocation: Map<string, Map<SensorMeasureType, SensorMeasureLatestDto>>
    private backendRxByLocation: Map<string, Observable<Map<SensorMeasureType, SensorMeasureLatestDto>>>;


    constructor(public rxStompService: RxStompService) {
        this.lastMeasuresByTypeByLocation = new Map();
        this.backendRxByLocation = new Map();
    }


    getLatestMeasuresAsync(newSensorKeys: SensorMeasureMetaData[], location: string): Observable<Map<SensorMeasureType, SensorMeasureLatestDto>> {
        const allowedSensorTypes: Set<SensorMeasureType> = new Set();
        newSensorKeys.forEach( sensorKey => allowedSensorTypes.add(sensorKey.type));

        this.connect(location);
        return this.backendRxByLocation
            .get(location)
            .pipe(map( latestMeasuresByType => this.mapWithTargetMeasures(latestMeasuresByType, allowedSensorTypes)));
    }

    private mapWithTargetMeasures(latestMeasuresByType : Map<SensorMeasureType, SensorMeasureLatestDto>, targetSensorTypes: Set<SensorMeasureType>) : Map<SensorMeasureType, SensorMeasureLatestDto>{
        const filteredMap = new Map()
        latestMeasuresByType.forEach( (value, key, _) => {
            if (targetSensorTypes.has(key)) {
                filteredMap.set(key, value);
            }
        })
        return filteredMap;
    }

    private connect(location: string) : void {
        if (!this.backendRxByLocation.has(location)){
            const backendRxForLocation = this.rxStompService
            .watch(this.measureReceiveRootEndpoint + "/" + location)
            .pipe(map( message => {
                const latestMeasure = SensorMeasureLatestDto.deepCopy(JSON.parse(message.body))
                return this.updateAndCopyLatestMeasuresMap(latestMeasure);
            }))
            this.backendRxByLocation.set(location, backendRxForLocation);
        }
    }

    private updateAndCopyLatestMeasuresMap(latestMeasure : SensorMeasureLatestDto) : Map<SensorMeasureType, SensorMeasureLatestDto> {
        var lastMeasuresByType = this.lastMeasuresByTypeByLocation.get(latestMeasure.location);

        if(!lastMeasuresByType) {
            lastMeasuresByType = new Map();
        }
        lastMeasuresByType.set(latestMeasure.type, latestMeasure);

        this.lastMeasuresByTypeByLocation.set(latestMeasure.location, lastMeasuresByType)
        return SensorLatestServiceCompanion.copyMeasuresMap(lastMeasuresByType);
    }

}