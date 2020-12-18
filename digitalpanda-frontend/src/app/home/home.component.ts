import { Component, OnDestroy, OnInit } from '@angular/core';
import { GreetingService } from './greeting.service';
import { Observable, Subscription} from 'rxjs';
import { Greeting } from './greeting.classes';
import { EchoSocketService } from './echo-socket.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit, OnDestroy {

  callerName: string;
  greeting: Greeting;
  greetingSubscription: Subscription;

  serverEcho: string;
  echoServiceSubscription: Subscription;

  constructor(public greetingService: GreetingService, public echoSocketService: EchoSocketService) {
    this.callerName = 'visitor';
    this.greeting = new Greeting(0, 'nothing');
  }

  ngOnDestroy(): void {
    this.greetingSubscription.unsubscribe();
    this.echoServiceSubscription.unsubscribe();
    this.echoSocketService.close();
  }

  ngOnInit(): void  {
    this.greetingSubscription = this.greetingService.getGreeting(this.callerName)
      .subscribe(greeting => {
        this.greeting = greeting;
      });


     this.echoServiceSubscription = this.echoSocketService.getMessageStream()
     .subscribe(
      msg => {  // Called whenever there is a message from the server.
        this.serverEcho = msg
        console.log('Message received from socket-server: ' + msg)
      },
      err => {
        console.log('Error with socket-server:' + err)
        this.serverEcho = "Connection error to server"
      },
      () => console.log('Connection to socket-server closed')
    )

    this.echoSocketService.sendMessage("hello stream")
  }
}
