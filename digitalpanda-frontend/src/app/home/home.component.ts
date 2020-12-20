import { Component, OnDestroy, OnInit } from '@angular/core';
import { GreetingService } from './greeting.service';
import { Subscription} from 'rxjs';
import { environment } from '../../environments/environment';
import { Greeting } from './greeting.classes';
import { EchoSocketService } from './echo-socket.service';
import { first } from 'rxjs/operators';
import {formatDate} from '@angular/common';


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

  get enableWebsocket(): boolean {
    return environment.enableWebsocket;
  }

  constructor(public greetingService: GreetingService, public echoSocketService: EchoSocketService) {
    this.callerName = 'visitor';
    this.greeting = new Greeting(0, 'nothing');
  }

  ngOnDestroy(): void {
    console.debug('HomeComponent.ngOnDestroy - begin')
    this.echoServiceSubscription && this.echoServiceSubscription.unsubscribe();
    console.debug('HomeComponent.ngOnDestroy - end')
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
    this.echoServiceSubscription = this.echoSocketService.getInputStream()
    .subscribe(
     msg => {
       this.serverEcho = msg
       console.debug('HomeComponent.echoServiceSubscription: Message received from socket-server: ' + msg)
     },
     err => {
       this.serverEcho = "HomeComponent.echoServiceSubscription: Connection error to server"
       console.error('Error with socket-server:' + err)
     },
     () => {
       console.debug('HomeComponent.echoServiceSubscription: Connection to socket-server closed')
     });

   this.echoSocketService.sendMessage("Hello stream @ " + formatDate(new Date(), 'yyyy/MM/dd HH:mm:ss', 'en'));
  }
}
