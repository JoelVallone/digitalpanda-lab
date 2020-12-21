import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map } from 'rxjs/operators';
import {Observable} from 'rxjs';
import { environment } from '../../environments/environment';
import { Greeting } from './greeting.classes';

@Injectable()
export class GreetingService {
  constructor(private http: HttpClient) {}

  getGreeting( name: string) {
    return this.makeRequest(name);
  }

  private makeRequest(name: string): Observable<Greeting> {
    const params = new HttpParams().set('name', name);
    const url = environment.httpApiEndpoint + `/ui/greeting`;
    return this.http.get<Greeting>(url, {params});
  }
}
