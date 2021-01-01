import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { NgModule } from '@angular/core';

// https://github.com/angular/angular-cli/wiki/stories-include-bootstrap
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import {
  InjectableRxStompConfig,
  RxStompService,
  rxStompServiceFactory,
} from '@stomp/ng2-stompjs';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

import { RouterModule } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import {
  LocationStrategy,
  HashLocationStrategy
} from '@angular/common';

import { GreetingService } from './home/greeting.service';
import { EchoNativeWebSocketService } from './home/ws-echo/echo-ws-native.service';
import { HomeComponent } from './home/home.component';
import { SensorModule } from './sensor/sensor.module';
import { UiModule } from './ui/ui.module';
import { Logger } from './core/logger';
import { SensorLatestWsService } from './sensor/service/ws/sensor-latest-ws.service';

@NgModule({
  declarations: [AppComponent, HomeComponent],
  imports: [BrowserModule, FormsModule, HttpClientModule, AppRoutingModule, NgbModule, SensorModule, UiModule],
  providers: [
    EchoNativeWebSocketService,
    GreetingService,
    { provide: LocationStrategy, useClass: HashLocationStrategy }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
  constructor() {
    Logger.debug('AppModule loaded.');
  }
}
