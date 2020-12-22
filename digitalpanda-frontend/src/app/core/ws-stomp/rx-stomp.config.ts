import { formatDate } from '@angular/common';
import { InjectableRxStompConfig } from '@stomp/ng2-stompjs';
import { environment } from 'src/environments/environment';
import { Logger } from './logger';

export const rxStompConfig: InjectableRxStompConfig = {
  // Which server?
  brokerURL: environment.wsApiEndpoint + environment.wsStompHandshakeEndpoint,

  // Headers
  // Typical keys: login, passcode, host
  connectHeaders: {
    login: '',
    passcode: '',
  },

  // How often to heartbeat?
  // Interval in milliseconds, set to 0 to disable
  heartbeatIncoming: 0, // Typical value 0 - disabled
  heartbeatOutgoing: 20000, // Typical value 20000 - every 20 seconds

  // Wait in milliseconds before attempting auto reconnect
  // Set to 0 to disable
  // Typical value 500 (500 milli seconds)
  reconnectDelay: 500,

  // Will log diagnostics on console
  // It can be quite verbose, not recommended in production
  // Skip this key to stop logging to console
  debug: (msg: string): void => {
    Logger.debug("[RxStomp-" +formatDate(new Date(), 'yyyy/MM/dd HH:mm:ss', 'en') + "] " +  msg);
  },
};
