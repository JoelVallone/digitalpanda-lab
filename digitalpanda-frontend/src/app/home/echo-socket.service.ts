import { Injectable, OnDestroy } from '@angular/core';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';
import { catchError, tap, switchAll } from 'rxjs/operators';
import { EMPTY, Observable, Subject } from 'rxjs';
import { environment } from 'src/environments/environment';

/*
  Example service for websocket backend connection.
  Works in pair with python echo backend server : see .md file in digitalpanda-study/web-socket-and-worker for setup
*/
@Injectable({
  providedIn: 'root'
})
export class EchoSocketService  implements OnDestroy {
  
  private echoEndpoint: string = environment.wsApiEndpoint + "/ui/websocket/echo" //"ws://localhost:9998/echo"
  private socket$: WebSocketSubject<string>;
  
  public connect(): void {
    if (!this.socket$ || this.socket$.closed) {
      console.debug("[EchoSocketService].connect() : new WebSocketSubject connection")
      this.socket$ =  webSocket({
        url: this.echoEndpoint
        , deserializer : e => String(e.data)
        , serializer : e => e
        , openObserver  : { next: () =>  console.debug("[EchoSocketService]: server connection opened")}
        , closeObserver : { next: () =>  console.debug("[EchoSocketService]: server connection closed")}
      });
  }
}

  sendMessage(message: string) {
    this.connect();
    this.socket$.next(message);
  }

  getInputStream(): Observable<string> {
    this.connect();
    return this.socket$.asObservable();
  }

  ngOnDestroy(): void {
    this.close();
  }

  close() {
    this.socket$.complete();
  }
}