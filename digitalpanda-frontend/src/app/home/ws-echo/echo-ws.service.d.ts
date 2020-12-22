import { Observable } from "rxjs";

export interface WsEchoService  {
  sendMessage(message: string) : void
  getInputStream(): Observable<string>
}