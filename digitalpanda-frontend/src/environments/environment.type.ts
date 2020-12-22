export interface Environment {
    production: boolean,
    httpApiEndpoint : string,
    wsApiEndpoint : string,
    wsStompHandshakeEndpoint: string,
    wsStompOutPrefix: string,
    wsStompInPrefix: string,
    enableWebsocket: boolean,
    debugLogs: boolean,
}