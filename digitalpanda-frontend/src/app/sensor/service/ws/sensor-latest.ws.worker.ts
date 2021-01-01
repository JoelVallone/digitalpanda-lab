/// <reference lib="webworker" />

import { SensorLatestWsServiceNative } from "./sensor-latest.ws.native-service";

import { RxStomp } from "@stomp/rx-stomp"
import { rxStompConfig } from "src/app/core/ws-stomp/rx-stomp.config";
import { Logger } from "src/app/core/logger";
import { Subscription, timer } from "rxjs";
import { WorkerTaskState, WorkerTaskUpdate } from "../sensor-latest.service";


class SensorLatestWsWorkerService {

  // If set to 0, enables fully reactive updates.
  // But then, cancels the advantage of the Webworker thread offloading the UI thread
  // during WebSocket (very) high throughput update spikes.
  private readonly UI_THREAD_REFRRESH_RATE: number = 1000;

  private rxStompClient: RxStomp;
  private sensorLatestWsServiceNative: SensorLatestWsServiceNative;
  private measureUpdateByLocation: Map<string, Subscription>

  constructor() {
    this.rxStompClient = this.initWsRxStompClient();
    this.sensorLatestWsServiceNative = new SensorLatestWsServiceNative(this.rxStompClient);
    this.measureUpdateByLocation = new Map();
    this.initUiThreadListener();
    this.initUiThreadTransmitter();
  }

  private initWsRxStompClient(): RxStomp {
    const rxStomp = new RxStomp();
    const config = rxStompConfig;
    config.debug = (msg: string): void => {
      Logger.debug("[Worker.RxStomp-" + new Date().toISOString() + "] " + msg);
    },
      rxStomp.configure(config);
    rxStomp.activate();

    return rxStomp;
  }

  private initUiThreadListener() {
    addEventListener('message', (event: MessageEvent) => this.handleNewUiThreadTaskUpdate(event));
  }

  private initUiThreadTransmitter() {
    if (this.UI_THREAD_REFRRESH_RATE > 0) {
      timer(0, this.UI_THREAD_REFRRESH_RATE)
        .subscribe((_) => { this.measureUpdateByLocation.size != 0 && this.sensorLatestWsServiceNative.lastMeasuresByTypeByLocation })
    }
  }

  private handleNewUiThreadTaskUpdate(event: MessageEvent) {
    Logger.debug("[Worker.RxStomp] received new UI event:" + JSON.stringify(event.data))
    if (!event.data) {
      return;
    }

    const jsonData = event.data;
    const taskTargetState = new WorkerTaskUpdate(WorkerTaskState[jsonData.state], jsonData.location)

    switch (taskTargetState.state) {
      case WorkerTaskState.LOADING_MEASURES: {
        this.subscribeToNewSensorSource(taskTargetState.location);
      }
      case WorkerTaskState.STOPPED: {
        this.unsubscribeFromSensorSource(taskTargetState.location);
      }
    }
  }

  private subscribeToNewSensorSource(location: string) {
    if (!this.measureUpdateByLocation.has(location)) {
      const subscription = this.sensorLatestWsServiceNative
        .getLatestMeasuresAsync([], location)
        .subscribe(measuresByType =>
          this.UI_THREAD_REFRRESH_RATE <= 0 && postMessage(JSON.stringify({ [location]: measuresByType })));
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


