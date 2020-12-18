import { Injectable } from '@angular/core';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';
import { environment } from '../../environments/environment';
import { catchError, tap, switchAll } from 'rxjs/operators';
import { EMPTY, Observable, Subject } from 'rxjs';
  
@Injectable({
  providedIn: 'root'
})
export class EchoSocketService {
  private socket$: WebSocketSubject<string>;
  
  public connect(): void {
    if (!this.socket$ || this.socket$.closed) {
      this.socket$ =  webSocket("ws://localhost:9998/echo");
    }
  }

  sendMessage(message: string) {
    this.connect();
    this.socket$.next(message);
  }

  getMessageStream(): Subject<string> {
    this.connect();
    return this.socket$;
  }

  close() {
    this.socket$.complete();
  }
}