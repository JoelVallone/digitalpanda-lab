import { Injectable, OnDestroy } from "@angular/core";
import { RxStompService } from "@stomp/ng2-stompjs";
import { Observable } from "rxjs";
import { environment } from "src/environments/environment";
import { SensorMeasureMetaData, SensorMeasureType, SensorMeasureLatestDto } from "../../sensor.classes";
import { SensorLatestService } from "../sensor-latest.service";
import { SensorLatestWsServiceNative } from "./sensor-latest.ws.native-service";
import { SensorLatestWsWorkerProxyService } from "./sensor-latest.ws.worker.proxy-service";

@Injectable({
    providedIn: 'root'
  })
export class SensorLatestWsService implements SensorLatestService, OnDestroy {

    private sensorLatestWsWorkerProxyService: SensorLatestWsWorkerProxyService;
    private sensorLatestWsServiceDelegate: SensorLatestService;

    constructor(public ngRxStompService: RxStompService) {
        if (SensorLatestWsWorkerProxyService.isWorkerAllowed()) {
            this.sensorLatestWsServiceDelegate = new SensorLatestWsWorkerProxyService();
        } else{
            this.sensorLatestWsServiceDelegate = new SensorLatestWsServiceNative(ngRxStompService)
        }
    }

    ngOnDestroy(): void {
        this.sensorLatestWsWorkerProxyService && this.sensorLatestWsWorkerProxyService.terminateWorker();
    }

    getLatestMeasuresAsync(newSensorKeys: SensorMeasureMetaData[], location: string): Observable<Map<SensorMeasureType, SensorMeasureLatestDto>> {
        return this.sensorLatestWsServiceDelegate.getLatestMeasuresAsync(newSensorKeys, location);
    }

}