export interface Environment {
    production: boolean,
    httpApiEndpoint : string,
    wsApiEndpoint : string,
    wsStompHandshakeEndpoint: string,
    wsStompPublishPrefix: string,
    wsStompSubscribePrefix: string,
    enableWebsocket: boolean,
    enableWebworker: boolean,
    debugLogs: boolean,
}