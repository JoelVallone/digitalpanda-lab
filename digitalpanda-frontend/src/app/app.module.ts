import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { NgModule } from '@angular/core';

// https://github.com/angular/angular-cli/wiki/stories-include-bootstrap
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';


import {RouterModule} from '@angular/router';
import {GreetingService} from './home/greeting.service';
import {HttpModule} from '@angular/http';
import {LocationStrategy, HashLocationStrategy} from '@angular/common';

import {HomeComponent} from './home/home.component';
import {SensorModule} from './sensor/sensor.module';
import {UiModule} from './ui/ui.module';

@NgModule({
  declarations: [AppComponent, HomeComponent],
  imports     : [BrowserModule, FormsModule, HttpModule, AppRoutingModule, NgbModule, SensorModule, UiModule],
  providers   : [GreetingService, {provide: LocationStrategy, useClass: HashLocationStrategy}],
  bootstrap   : [AppComponent]
})
export class AppModule { }
