import { Injectable, OnDestroy } from "@angular/core";
import { Observable } from "rxjs";
import { ConfigHelper } from "src/app/core/config-helper";
import { RxStompClient } from "src/app/core/ws-stomp/rx-stomp.client";
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

    constructor() {
        if (ConfigHelper.isWebWorkerAllowed()) {
            this.sensorLatestWsServiceDelegate = new SensorLatestWsWorkerProxyService();
        } else{
            this.sensorLatestWsServiceDelegate = new SensorLatestWsServiceNative(RxStompClient.loadWsRxStompClientSingleton())
        }
    }

    ngOnDestroy(): void {
        this.sensorLatestWsWorkerProxyService && this.sensorLatestWsWorkerProxyService.terminateWorker();
    }

    getLatestMeasuresAsync(newSensorKeys: SensorMeasureMetaData[], location: string): Observable<Map<SensorMeasureType, SensorMeasureLatestDto>> {
        return this.sensorLatestWsServiceDelegate.getLatestMeasuresAsync(newSensorKeys, location);
    }

}