import { Observable } from "rxjs";
import { SensorMeasureLatestDto, SensorMeasureMetaData, SensorMeasureType } from "../sensor.classes";

export interface SensorLatestService {
    getLatestMeasuresAsync(newSensorKeys: Array<SensorMeasureMetaData>, location: string) : Observable<Map<SensorMeasureType, SensorMeasureLatestDto>>;
}

export class SensorLatestServiceCompanion {

    public static copyMeasuresMap(original: Map<SensorMeasureType, SensorMeasureLatestDto>): Map<SensorMeasureType, SensorMeasureLatestDto> {
        const copy = new Map();
        original.forEach((value, key, _map) => {
            copy.set(key, SensorMeasureLatestDto.deepCopy(value));
        });
        return copy;
    }
}