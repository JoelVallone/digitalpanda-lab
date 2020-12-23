import { Component, OnDestroy, OnInit } from '@angular/core';
import { GreetingService } from './greeting.service';
import { Subscription} from 'rxjs';
import { environment } from '../../environments/environment';
import { Greeting } from './greeting.classes';
import { EchoNativeWebSocketService } from './ws-echo/echo-ws-native.service';
import { first } from 'rxjs/operators';
import {formatDate} from '@angular/common';
import { EchoStompWebSocketService } from './ws-echo/echo-ws-stomp.service';
import { WsEchoService } from './ws-echo/echo-ws.service';
import { Logger } from '../core/ws-stomp/logger';


@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit, OnDestroy {

  callerName: string;
  greeting: Greeting;

  serverEcho: string;
  echoServiceSubscription: Subscription;
  echoWsService: WsEchoService;

  get enableWebsocket(): boolean {
    return environment.enableWebsocket;
  }

  constructor(
    public greetingService: GreetingService, 
    public echoNativeSocketService: EchoNativeWebSocketService,
    public echoStompSocketService: EchoStompWebSocketService) {
    
    this.echoWsService = echoNativeSocketService;
    //this.echoWsService = echoStompSocketService;
    this.callerName = 'visitor';
    this.greeting = new Greeting(0, 'nothing');
  }

  ngOnDestroy(): void {
    this.echoServiceSubscription && this.echoServiceSubscription.unsubscribe();
  }

  ngOnInit(): void  {
    this.greetingService.getGreeting(this.callerName)
    .pipe(first())
    .subscribe(greeting => {
        this.greeting = greeting;
      });


    if (environment.enableWebsocket) {
      this.initWebsocketEchoSubscription();
    }
  }

  private initWebsocketEchoSubscription() {
    this.echoServiceSubscription = this.echoWsService.getInputStream()
      .subscribe(
        msg => {
          this.serverEcho = msg
          Logger.debug('HomeComponent.echoServiceSubscription: Message received from socket-server: ' + msg)
        },
        err => {
          this.serverEcho = "HomeComponent.echoServiceSubscription: Connection error to server"
          Logger.error('Error with socket-server:' + err)
        },
        () => {
          Logger.debug('HomeComponent.echoServiceSubscription: Connection to socket-server closed')
        }
      );

   this.echoWsService
    .sendMessage("Hello backend, it is " + formatDate(new Date(), 'yyyy/MM/dd HH:mm:ss', 'en'));
  }
}
