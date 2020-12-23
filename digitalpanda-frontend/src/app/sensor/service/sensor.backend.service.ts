import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { first, map } from 'rxjs/operators';
import {Observable} from 'rxjs';
import { SensorMeasureMetaData, SensorMeasureLatestDto, SensorMeasuresHistoryDto } from '../sensor.classes';
import { environment } from '../../../environments/environment';

@Injectable()
export class SensorBackendService {
  public static baseUrl: string  =  environment.httpApiEndpoint + `/ui/sensor`;

  constructor(private http: HttpClient) {}

  loadHistoryMeasures(measureKey: SensorMeasureMetaData, startTimeMillisIncl: number,
                      endTimeMillisExcl: number, dataPointCount: number): Observable<Array<SensorMeasuresHistoryDto>> {
    const params = new HttpParams()
      .set('type', JSON.stringify(measureKey.type).replace(/\"/g, ''))
      .set('location', measureKey.location)
      .set('startTimeMillisIncl', String(startTimeMillisIncl))
      .set('endTimeMillisExcl', String(endTimeMillisExcl))
      .set('dataPointCount', String(this.cappedDatapointCount(startTimeMillisIncl, endTimeMillisExcl, dataPointCount)));
    return this.http.get<Array<SensorMeasuresHistoryDto>>(SensorBackendService.baseUrl + `/history`, {params}).pipe(first());
  }

  cappedDatapointCount(startTimeMillisIncl: number, endTimeMillisExcl: number,
                      dataPointCount: number): number {
     const timeIntervalMillis = endTimeMillisExcl - startTimeMillisIncl;
     if ((timeIntervalMillis / dataPointCount) < 1000) {
        return Math.max(Math.floor(timeIntervalMillis / 1000), 1);
     }
     return dataPointCount;
  }

  loadLatestMeasureOnce(measureKey: SensorMeasureMetaData): Observable<SensorMeasureLatestDto> {
    const params = new HttpParams()
      .set('type', JSON.stringify(measureKey.type).replace(/\"/g, ''))
      .set('location', measureKey.location);
    return this.http.get<SensorMeasureLatestDto>(SensorBackendService.baseUrl, {params}).pipe(first())
  }

  loadMeasurekeys(): Observable<Array<SensorMeasureMetaData>> {
    const url: string = SensorBackendService.baseUrl + `/keys`;
    return this.http.get<Array<SensorMeasureMetaData>>(url).pipe(first());
  }
}
