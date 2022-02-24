import { BehaviorSubject, Observable, Subject } from "rxjs";
import { ConfigHelper } from "src/app/core/config-helper";
import { Logger } from "src/app/core/logger";
import { environment } from "src/environments/environment";
import { SensorMeasureMetaData, SensorMeasureType, SensorMeasureLatestDto } from "../../sensor.classes";
import { SensorLatestService, WorkerTaskState, WorkerTaskUpdate } from "../sensor-latest.service";
import { SensorLatestWsServiceNative } from "./sensor-latest.ws.native-service";


export class SensorLatestWsWorkerProxyService implements SensorLatestService {

    private worker: Worker;
    private lastMeasuresByType$ByLocation: Map<string, BehaviorSubject<Map<SensorMeasureType, SensorMeasureLatestDto>>>

    constructor() {
        this.worker = this.initWorkerThread();
        this.lastMeasuresByType$ByLocation = new Map();
    }

    terminateWorker(): void {
        this.worker && this.worker.terminate();
    }

    private initWorkerThread(): Worker {
        if (ConfigHelper.isWebWorkerAllowed()) {
            const worker = new Worker(new URL('./sensor-latest.ws.worker', import.meta.url), { type: 'module' });
            worker.onmessage = (event) => this.handleSensorUpdate(event);
            return worker;
        } else {
            // Web Workers are not supported in this environment.
            console.error('Web Workers are not supported in this environment or disabled: SensorLatestWsWorkerProxyService will remain inactive');
        }
    }

    private handleSensorUpdate(event: MessageEvent) {
        const jsonText = event.data
        const measuresByLocationByTypeRaw = JSON.parse(jsonText)
        for (const [location, measuresByTypeRaw] of Object.entries(measuresByLocationByTypeRaw)) {
            const latestMeasureByType = SensorLatestWsWorkerProxyService.deserialize(measuresByTypeRaw)
            const lastMeasuresByType$ = this.lastMeasuresByType$ByLocation.get(location);
            if (!lastMeasuresByType$) {
                continue; //already sent delete message for location to worker
            } else if (lastMeasuresByType$.observers.length > 0) {
                lastMeasuresByType$.next(latestMeasureByType);
            } else {
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
        const measureByType$ = this.getOrCreateLastMeasuresByType$(location);
        return SensorLatestWsServiceNative.filterLatestMeasuresAsync(measureByType$, newSensorKeys)
    }

    private getOrCreateLastMeasuresByType$(location: string): Subject<Map<SensorMeasureType, SensorMeasureLatestDto>> {
        if (!this.lastMeasuresByType$ByLocation.has(location)) {
            this.lastMeasuresByType$ByLocation.set(location, new BehaviorSubject(new Map()));
            this.worker.postMessage(new WorkerTaskUpdate(WorkerTaskState.LOADING_MEASURES, location))
        }
        return this.lastMeasuresByType$ByLocation.get(location);
    }

    private deleteLastMeasuresByType$(location: string) {
        this.worker.postMessage(new WorkerTaskUpdate(WorkerTaskState.STOPPED, location));
        this.lastMeasuresByType$ByLocation.delete(location);
    }
}
