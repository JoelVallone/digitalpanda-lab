import { Injectable, OnDestroy } from '@angular/core';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';
import {  Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { WsEchoService } from './echo-ws.service';
import { Logger } from 'src/app/core/ws-stomp/logger';

/*
  Example service for websocket backend connection.
  Works in pair with either:
    - python echo backend server. See .md file in digitalpanda-study/web-socket-and-worker for setup at "ws://localhost:9998/echo"
    - digitalpanda-backend text-based bidirectoninal WebSocket controller at `${environment.wsApiEndpoint}/ui/websocket/echo`
*/
@Injectable({
  providedIn: 'root'
})
export class EchoNativeWebSocketService implements OnDestroy, WsEchoService{
  
  private echoEndpoint: string = environment.wsApiEndpoint + "/ws/echo" //"ws://localhost:9998/echo"
  private socket$: WebSocketSubject<string>;
  
  public connect(): void {
    if (!this.socket$ || this.socket$.closed) {
      Logger.debug("[EchoSocketService].connect() : new WebSocketSubject connection")
      this.socket$ =  webSocket({
        url: this.echoEndpoint
        , deserializer : e => String(e.data)
        , serializer : e => e
        , openObserver  : { next: () =>  Logger.debug("[EchoSocketService]: server connection opened")}
        , closeObserver : { next: () =>  Logger.debug("[EchoSocketService]: server connection closed")}
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