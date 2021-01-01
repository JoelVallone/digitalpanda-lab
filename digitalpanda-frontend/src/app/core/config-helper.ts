import { environment } from "src/environments/environment";

export class ConfigHelper {

    public static isWebSocketAllowed(): boolean {
        return !!environment.enableWebsocket && !!window.WebSocket;
    }

    public static isWebWorkerAllowed(): boolean {
        return environment.enableWebworker && typeof Worker !== 'undefined';
    }
}