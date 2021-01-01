import { RxStomp } from "@stomp/rx-stomp";
import { environment } from "src/environments/environment";
import { Logger } from "../logger";
import { rxStompConfig } from "./rx-stomp.config";

export class RxStompClient {

    private static rxStomp;

    public static loadWsRxStompClientSingleton(logPrefix:string='RxStomp', rxStompConf=rxStompConfig): RxStomp {
        if (!RxStompClient.rxStomp) {
            RxStompClient.rxStomp = new RxStomp();
            const config = rxStompConf;
            config.debug = (msg: string): void => {
                //Logger.debug(`[${logPrefix} - ${new Date().toISOString()}] ${msg}`);
            },
            RxStompClient.rxStomp.configure(config);
            RxStompClient.rxStomp.activate();
        }
        return RxStompClient.rxStomp;
    }
}