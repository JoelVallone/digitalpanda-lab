// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

import { Environment } from "./environment.type";

export const environment: Environment = {
  production: false,
  httpApiEndpoint: 'http://digitalpanda.org:8081',
  wsApiEndpoint: 'ws://digitalpanda.org:8081',
  //httpApiEndpoint: 'http://127.0.0.1:8081',
  //wsApiEndpoint: 'ws://127.0.0.1:8081',
  wsStompHandshakeEndpoint: "/ws/stomp/handshake",
  wsStompPublishPrefix: "/ws/stomp/frontend-input",
  wsStompSubscribePrefix: "/ws/stomp/backend-output",
  enableWebsocket: true,
  enableWebworker: true,
  debugLogs: true
};

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/plugins/zone-error';  // Included with Angular CLI.
