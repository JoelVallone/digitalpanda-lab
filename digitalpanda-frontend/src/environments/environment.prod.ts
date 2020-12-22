import { Environment } from "./environment.type";

export const environment : Environment = {
  production: true,
  httpApiEndpoint: 'http://digitalpanda.org:8081',
  wsApiEndpoint: 'ws://digitalpanda.org:8081',
  wsStompHandshakeEndpoint: "/ws-hanshake",
  wsStompOutPrefix: "/ui-input",
  wsStompInPrefix: "/backend-output",
  enableWebsocket: false,
  debugLogs: false
};
