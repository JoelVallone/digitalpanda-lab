import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { LiveSensorComponent } from './sensor/live/live.sensor.component';
import { HistorySensorComponent } from './sensor/history/history.sensor.component';

const routes: Routes = [
  {path: '', redirectTo: 'home', pathMatch: 'full'},
  {path: 'home', component: HomeComponent},
  {path: 'monitoring/live', component: LiveSensorComponent},
  {path: 'monitoring/history', component: HistorySensorComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
