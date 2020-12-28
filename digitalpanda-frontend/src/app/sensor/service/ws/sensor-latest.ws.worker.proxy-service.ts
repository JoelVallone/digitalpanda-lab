import { BehaviorSubject, Observable, Subject } from "rxjs";
import { Logger } from "src/app/core/logger";
import { environment } from "src/environments/environment";
import { SensorMeasureMetaData, SensorMeasureType, SensorMeasureLatestDto } from "../../sensor.classes";
import { SensorLatestService } from "../sensor-latest.service";
import { SensorLatestWsServiceNative } from "./sensor-latest.ws.native-service";
import { WorkerTaskState, WorkerTaskUpdate } from "./sensor-latest.ws.worker";

export class SensorLatestWsWorkerProxyService implements SensorLatestService {

    private worker: Worker;
    private lastMeasuresByType$ByLocation: Map<string, BehaviorSubject<Map<SensorMeasureType, SensorMeasureLatestDto>>>

    constructor() {
        this.worker = this.initWorkerThread();
        this.lastMeasuresByType$ByLocation = new Map();
    }

    terminateWorker(): void {
        this.worker &&  this.worker.terminate();
    }

    private initWorkerThread(): Worker {
        if (SensorLatestWsWorkerProxyService.isWorkerAllowed()) {
            const worker = new Worker('./sensor/service/ws/sensor-latest.ws.worker', { type: 'module' });
            worker.onmessage = (event) => this.handleSensorUpdate(event);
            return worker;
        } else {
            // Web Workers are not supported in this environment.
            console.error('Web Workers are not supported in this environment or disabled: SensorLatestWsWorkerProxyService will remain inactive');
        }
    }

    public static isWorkerAllowed(): boolean {
        return environment.enableWebworker && typeof Worker !== 'undefined';
    }

    private handleSensorUpdate(event: MessageEvent) {
        //TODO: handle 0 ui listener case
        const jsonText = event.data
        const measuresByLocationByTypeRaw = JSON.parse(jsonText)
        Logger.debug(`UI Thread got update: ${measuresByLocationByTypeRaw}`);
        for (const [location, measuresByTypeRaw] of Object.entries(measuresByLocationByTypeRaw)) {
            const latestMeasureByType = SensorLatestWsWorkerProxyService.deserialize(measuresByTypeRaw)
            const lastMeasuresByType$ = this.getLastMeasuresByType$(location);
            if (!lastMeasuresByType$.closed) {
                lastMeasuresByType$.next(latestMeasureByType);
            }else {
                this.deleteLastMeasuresByType$(location);
            }
        }
    }

    public static deserialize(measuresByTypeRaw: any): Map<SensorMeasureType, SensorMeasureLatestDto> {
        const latestMeasureByType: Map<SensorMeasureType, SensorMeasureLatestDto> = new Map();
        for (const [key, value] of Object.entries(measuresByTypeRaw)) {
            latestMeasureByType.set(SensorMeasureType[key], SensorMeasureLatestDto.deepCopy(value))
        }
        return latestMeasureByType;
    }

    getLatestMeasuresAsync(newSensorKeys: SensorMeasureMetaData[], location: string): Observable<Map<SensorMeasureType, SensorMeasureLatestDto>> {
        const measureByType$ = this.getLastMeasuresByType$(location);
        return SensorLatestWsServiceNative.filterLatestMeasuresAsync(measureByType$, newSensorKeys)
    }

    private getLastMeasuresByType$(location: string): Subject<Map<SensorMeasureType, SensorMeasureLatestDto>> {
        if (!this.lastMeasuresByType$ByLocation.has(location)) {
            this.lastMeasuresByType$ByLocation.set(location, new BehaviorSubject(new Map()));
            this.worker.postMessage(new WorkerTaskUpdate(WorkerTaskState.LOADING_MEASURES, location))
        }
        return this.lastMeasuresByType$ByLocation.get(location);
    }

    private deleteLastMeasuresByType$(location: string){
        this.worker.postMessage(new WorkerTaskUpdate(WorkerTaskState.STOPPED, location));
        this.lastMeasuresByType$ByLocation.delete(location);
    }
}