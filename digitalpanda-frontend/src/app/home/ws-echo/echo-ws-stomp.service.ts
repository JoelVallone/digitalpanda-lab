import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { RxStompService } from '@stomp/ng2-stompjs';
import { Message } from '@stomp/stompjs';
import { map } from 'rxjs/operators';
import { WsEchoService } from './echo-ws.service';

/*
  Example service for websocket backend connection.
  Works in pair with python echo backend server : see .md file in digitalpanda-study/web-socket-and-worker for setup
  
  Refs: 
  - https://stomp-js.github.io/guide/ng2-stompjs/ng2-stomp-with-angular7.html
*/
@Injectable({
  providedIn: 'root'
})
export class EchoStompWebSocketService implements WsEchoService {
  
  private readonly echoSendEndpoint: string = environment.wsStompPublishPrefix + "/echo";
  private readonly echoReceiveEndpoint: string = environment.wsStompSubscribePrefix + "/echo";

  private $backendRx: Observable<Message>;
  
  constructor(public rxStompService: RxStompService) { }

  public connect(): void {
    if(!this.$backendRx){
      this.$backendRx = this.rxStompService.watch(this.echoReceiveEndpoint);
    }
  }

  sendMessage(message: string): void {
    this.rxStompService
      .publish({destination: this.echoSendEndpoint, body: message})
  }

  getInputStream(): Observable<string> {
    this.connect();
    return this.$backendRx
      .pipe(map(message => message.body));
  }
}