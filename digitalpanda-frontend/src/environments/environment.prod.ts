import { Environment } from "./environment.type";

export const environment : Environment = {
  production: true,
  httpApiEndpoint: 'http://digitalpanda.org:8081',
  wsApiEndpoint: 'ws://digitalpanda.org:8081',
  wsStompHandshakeEndpoint: "/ws/stomp/handshake",
  wsStompPublishPrefix: "/ws/stomp/frontend-input",
  wsStompSubscribePrefix: "/ws/stomp/backend-output",
  enableWebsocket: false,
  enableWebworker: false,
  debugLogs: false
};
