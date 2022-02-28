

import { SensorLatestWsServiceNative } from "./sensor-latest.ws.native-service";

import { Logger } from "src/app/core/logger";
import { Subscription, timer } from "rxjs";
import { WorkerTaskState, WorkerTaskUpdate } from "../sensor-latest.service";
import { RxStompClient } from "src/app/core/ws-stomp/rx-stomp.client";
import { SensorMeasureLatestDto, SensorMeasureType } from "../../sensor.classes";

class SensorLatestWsWorkerService {

  // If set to 0, enables fully reactive updates.
  // But then, cancels the advantage of the Webworker thread offloading the UI thread
  // during WebSocket (very) high throughput update spikes.
  private readonly UI_THREAD_REFRRESH_RATE: number = 1000;

  private sensorLatestWsServiceNative: SensorLatestWsServiceNative;
  private measureUpdateByLocation: Map<string, Subscription>

  constructor() {
    this.sensorLatestWsServiceNative = new SensorLatestWsServiceNative(RxStompClient.loadWsRxStompClientSingleton('Worker.RxStomp'));
    this.measureUpdateByLocation = new Map();
    this.initUiThreadListener();
    this.initUiThreadTransmitter();
  }

  private initUiThreadListener() {
    addEventListener('message', (event: MessageEvent) => this.handleNewUiThreadTaskUpdate(event));
  }

  private initUiThreadTransmitter() {
    if (this.UI_THREAD_REFRRESH_RATE > 0) {
      timer(0, this.UI_THREAD_REFRRESH_RATE)
        .subscribe((_) => {
          // A Typescript Map must be converted into a JS object to be properly stringified by JSON
          this.measureUpdateByLocation.size != 0 && postMessage(this.stringifyLocationsMap(this.sensorLatestWsServiceNative.lastMeasuresByTypeByLocation));
        });
    }
  }

  private stringifyLocationsMap(lastMeasuresByTypeByLocation: Map<string, Map<SensorMeasureType, SensorMeasureLatestDto>>): string {
    return JSON.stringify(
      Array
      .from(lastMeasuresByTypeByLocation.entries())
      .reduce((main, [location, lastMeasuresByType]) =>
          ({...main, [location]: this.stringifyMeasureTypesMap(lastMeasuresByType)}), {})
    )
  }

  private stringifyMeasureTypesMap(lastMeasuresByType: Map<SensorMeasureType, SensorMeasureLatestDto>) {
    return Array.from(lastMeasuresByType.entries()).reduce((main, [type, measure]) => ({...main, [type]: measure}), {})
  }

  private handleNewUiThreadTaskUpdate(event: MessageEvent) {
    Logger.debug("[Worker] received new UI event:" + JSON.stringify(event.data))
    if (!event.data) {
      return;
    }

    const jsonData = event.data;
    const taskTargetState = new WorkerTaskUpdate(WorkerTaskState[jsonData.state], jsonData.location)

    switch (taskTargetState.state) {
      case WorkerTaskState.LOADING_MEASURES: {
        this.subscribeToNewSensorSource(taskTargetState.location);
        break;
      }
      case WorkerTaskState.STOPPED: {
        this.unsubscribeFromSensorSource(taskTargetState.location);
        break;
      }
    }
  }

  private subscribeToNewSensorSource(location: string) {
    if (!this.measureUpdateByLocation.has(location)) {
      const subscription = this.sensorLatestWsServiceNative
        .getLatestMeasuresAsync([], location)
        .subscribe(measuresByType => {
          // A Typescript Map must be converted into a JS object to be properly stringified by JSON
          const payload = JSON.stringify({ [location]: this.stringifyMeasureTypesMap(measuresByType) });
          this.UI_THREAD_REFRRESH_RATE <= 0 && postMessage(payload);
        });
      this.measureUpdateByLocation.set(location, subscription)
    }
  }

  private unsubscribeFromSensorSource(location: string) {
    if (this.measureUpdateByLocation.has(location)) {
      this.measureUpdateByLocation.get(location).unsubscribe();
      this.measureUpdateByLocation.delete(location)
    }
  }
}

new SensorLatestWsWorkerService();


