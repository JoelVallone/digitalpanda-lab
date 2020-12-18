import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map } from 'rxjs/operators';
import {Observable} from 'rxjs';
import { SensorMeasureMetaData, SensorMeasureLatestDto, SensorMeasuresHistoryDto } from './sensor.classes';
import { environment } from './../../environments/environment';

@Injectable()
export class SensorService {
  public static baseUrl: string  =  environment.APIEndpoint + `/ui/sensor`;

  constructor(private http: HttpClient) {}

  loadHistoryMeasures(measureKey: SensorMeasureMetaData, startTimeMillisIncl: number,
                      endTimeMillisExcl: number, dataPointCount: number): Observable<Array<SensorMeasuresHistoryDto>> {
    const params = new HttpParams()
      .set('type', JSON.stringify(measureKey.type).replace(/\"/g, ''))
      .set('location', measureKey.location)
      .set('startTimeMillisIncl', String(startTimeMillisIncl))
      .set('endTimeMillisExcl', String(endTimeMillisExcl))
      .set('dataPointCount', String(this.cappedDatapointCount(startTimeMillisIncl, endTimeMillisExcl, dataPointCount)));
    return this.http.get<Array<SensorMeasuresHistoryDto>>(SensorService.baseUrl + `/history`, {params});
  }

  cappedDatapointCount(startTimeMillisIncl: number, endTimeMillisExcl: number,
                      dataPointCount: number): number {
     const timeIntervalMillis = endTimeMillisExcl - startTimeMillisIncl;
     if ((timeIntervalMillis / dataPointCount) < 1000) {
        return Math.max(Math.floor(timeIntervalMillis / 1000), 1);
     }
     return dataPointCount;
  }
  loadLatestMeasure(measureKey: SensorMeasureMetaData): Observable<SensorMeasureLatestDto> {
    const params = new HttpParams()
      .set('type', JSON.stringify(measureKey.type).replace(/\"/g, ''))
      .set('location', measureKey.location);
    return this.http.get<SensorMeasureLatestDto>(SensorService.baseUrl, {params})
  }

  loadMeasurekeys(): Observable<Array<SensorMeasureMetaData>> {
    const url: string = SensorService.baseUrl + `/keys`;
    return this.http.get<Array<SensorMeasureMetaData>>(url);
  }
}
