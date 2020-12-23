import { Observable, timer } from "rxjs";
import { map, mergeMap } from "rxjs/operators";
import { SensorMeasureLatestDto, SensorMeasureMetaData, SensorMeasureType } from "../sensor.classes";
import { SensorBackendService } from "./sensor.backend.service";
import { SensorLatestService, SensorLatestServiceCompanion } from "./sensor-latest.service";

export class SensorLatestHttpPollingService implements SensorLatestService {

    public periodicServiceCallHandle?: any;

    private latestMeasures$: Observable<Map<SensorMeasureType, SensorMeasureLatestDto>>
    private lastMeasures: Map<SensorMeasureType, SensorMeasureLatestDto>
    private sensorKeys: Array<SensorMeasureMetaData>

    constructor(public sensorBackendService: SensorBackendService){
        this.lastMeasures = new Map();
        this.sensorKeys = new Array();
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
                return SensorLatestServiceCompanion.copyMeasuresMap(this.lastMeasures);
            }))
    }

    public getLatestMeasuresAsync(newSensorKeys: Array<SensorMeasureMetaData>, location: string) : Observable<Map<SensorMeasureType, SensorMeasureLatestDto>> {
        this.sensorKeys = newSensorKeys.filter((sensorKey) => sensorKey.location === location)
        this.lastMeasures = new Map();
        return this.latestMeasures$
    }
}