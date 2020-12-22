import { Injectable, OnDestroy } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { RxStompService, StompConfig, StompService } from '@stomp/ng2-stompjs';
import { Message, IMessage} from '@stomp/stompjs';
import { map } from 'rxjs/operators';
import { rxStompConfig } from '../../core/ws-stomp/rx-stomp.config';
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
  
  private echoSendEndpoint: string = environment.wsStompOutPrefix + "/echo";
  private echoReceiveEndpoint: string = environment.wsStompInPrefix + "/echo";

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