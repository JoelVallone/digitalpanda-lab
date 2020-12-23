import { BehaviorSubject, concat, timer } from "rxjs";
import { Subject } from "rxjs";
import { Observable } from "rxjs";
import { concatMap, flatMap, map, mergeMap } from "rxjs/operators";
import { ViewMeasure } from "../live/location-values-display/location-values-display.latest.component";
import { SensorMeasureLatestDto, SensorMeasureMetaData, SensorMeasureType, SensorMeasureTypeDetails } from "../sensor.classes";
import { SensorBackendService } from "./sensor.backend.service";




export class SensorLatestService {

    public periodicServiceCallHandle?: any;

    private latestMeasures$: Observable<Map<SensorMeasureType, SensorMeasureLatestDto>>
    private lastMeasures: Map<SensorMeasureType, SensorMeasureLatestDto>
    private sensorKeys: Array<SensorMeasureMetaData>

    constructor(public sensorBackendService: SensorBackendService){
        this.lastMeasures = new Map();
        this.latestMeasures$ = timer(0, 1000).pipe(
             mergeMap( _ => this.getAllLatestMeasuresOnce()),
             mergeMap( measureUpdate$ =>  this.updateAndCopyLatestMeasuresMap(measureUpdate$)));
    }

    private getAllLatestMeasuresOnce(): Array<Observable<SensorMeasureLatestDto>> {
        return this.sensorKeys.map((measureKey) => this.sensorBackendService.loadLatestMeasureOnce(measureKey))
    }

    private updateAndCopyLatestMeasuresMap(measureUpdate$: Observable<SensorMeasureLatestDto> ): Observable<Map<SensorMeasureType, SensorMeasureLatestDto>> {
        return measureUpdate$.pipe(
            map(latestMeasure => {
                this.lastMeasures.set(latestMeasure.type, latestMeasure)
                return SensorLatestService.copyMeasuresMap(this.lastMeasures);
            }))
    }

    public getLatestMeasuresAsync() : Observable<Map<SensorMeasureType, SensorMeasureLatestDto>> {
        return this.latestMeasures$
    }

    public updateTargetMeasures(newSensorKeys: Array<SensorMeasureMetaData>, location: string): void {
        this.sensorKeys = newSensorKeys.filter((sensorKey) => sensorKey.location === location)
        this.lastMeasures = new Map();
    }

    private static copyMeasuresMap(original: Map<SensorMeasureType, SensorMeasureLatestDto>): Map<SensorMeasureType, SensorMeasureLatestDto>{
        const copy = new Map();
        original.forEach((value, key, _map) => {
            copy.set(key, SensorMeasureLatestDto.deepCopy(value));
        });
        return copy;
    }
}