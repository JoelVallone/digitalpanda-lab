import { Injectable } from '@angular/core';
import { Http, URLSearchParams } from '@angular/http';
import { map } from 'rxjs/operators';
import {Observable} from 'rxjs';
import { environment } from './../../environments/environment';
import { Greeting } from './greeting.classes';

@Injectable()
export class GreetingService {
  constructor(private http: Http) {}

  getGreeting( name: string) {
    return this.makeRequest(name);
  }

  private makeRequest(name: string): Observable<Greeting> {
    const params = new URLSearchParams();
    params.set('name', name);
    const url = environment.APIEndpoint + `/ui/greeting`;
    return this.http.get(url, {search: params})
      .pipe(map((res) => res.json() as Greeting));
  }
}
