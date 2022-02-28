export class SensorMeasureMean {
  public constructor(
    public value: number,
    public refTimestamp: number,
    public startTimestampIncl: number,
    public endTimestampExcl: number) { }
}

export class SensorMeasuresHistoryDto {
  public constructor(
    public startTimeMillisIncl: number,
    public endTimeMillisIncl: number,
    public timeMillisBetweenDataPoints: number,
    public location: string,
    public type: SensorMeasureType,
    public values: Array<number>
  ) { }
}

export class SensorMeasureLatestDto {

  public constructor(public location: string, public type: SensorMeasureType, public value: number, public timestamp: number) { }

  public static deepCopy(that): SensorMeasureLatestDto {
    return new SensorMeasureLatestDto(that.location, that.type, that.value, that.timestamp)
  }
}

export class SensorMeasureTypeDetails {
  constructor(public typeName: string, public unitName: string, public unitSymbol: string) { }
  public toString = (): string => this.typeName + ' [' + this.unitSymbol + ']';
}

export enum SensorMeasureType {
  TEMPERATURE = 'TEMPERATURE',
  HUMIDITY = 'HUMIDITY',
  PRESSURE = 'PRESSURE',
  eCO2 = 'eCO2',
  TVOC = 'TVOC'
}

export class SensorMeasureMetaData {
  private static typeDetails  = {
     TEMPERATURE : new SensorMeasureTypeDetails('Temperature', 'Degree Celcius', '°C'),
     HUMIDITY :  new SensorMeasureTypeDetails('Humidity', 'Percentage', '%'),
     PRESSURE : new SensorMeasureTypeDetails('Pressure', 'Hecto-Pascal', 'hPa'),
     eCO2 : new SensorMeasureTypeDetails('Equivalent CO2 particle', 'Part Per Million', 'ppm'),
     TVOC : new SensorMeasureTypeDetails('Total Volatile Organic Compounds', 'Part Per Million', 'ppm')
   };

  public constructor(public location: string, public type: SensorMeasureType) { }

  public static getTypeDetail(type: SensorMeasureType):  SensorMeasureTypeDetails {
      return SensorMeasureMetaData.typeDetails[type];
  }
}
