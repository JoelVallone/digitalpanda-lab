import { environment } from "src/environments/environment"

export class Logger {

    static error(message: String): void {
        console.error(message)
    }

    static info(message: String): void {
        console.log(message)
    }

    static debug(message: String): void {
        environment.debugLogs && console.debug(message)
    }
}