# Air quality sensor
## Adafruit SGP30 
buy: https://www.play-zone.ch/en/adafruit-sgp30-air-quality-sensor-breakout-voc-and-eco2.html
C++ driver: https://github.com/adafruit/Adafruit_SGP30
Java driver: https://github.com/vkaam/SGP30-Java/blob/master/src/main/java/abc/SGP30.java
guide: https://learn.adafruit.com/adafruit-sgp30-gas-tvoc-eco2-mox-sensor?view=all
eCO2 (equivalent calculated carbon-dioxide) concentration within a range of 400 to 60,000 parts per million (ppm) (CO2 is calculated based on H2 concentration, it is not a 'true' CO2 sensor for laboratory use), 
TVOC (Total Volatile Organic Compound) concentration within a range of 0 to 60,000 parts per billion (ppb).
15% accuracy when calibrated
WARNING: baseline calibration takes 12 hours, then re-init with baseline stored on external device
    The 'problem' with these sensors is that the baseline changes, often with humidity, temperature, and other non-gas-related-events. 
    To keep the values coming out reasonable, you'll need to calibrate the sensor.
    Once the baseline is properly initialized or restored, the current baseline value should be stored approximately once per hour. 
    While the sensor is off, baseline values are valid for a maximum of seven days.
I2C address 0x58 
29.9.- CHF

# Meteo sensor
## Adafruit BME280
buy: https://www.play-zone.ch/en/elektronik-kit-zubehoer/sensoren/wetter-temperatur/adafruit-bme280-i2c-spi-temperature-humidity-pressure-sensor.html
C++ library: https://github.com/adafruit/Adafruit_BME280_Library
humidity with ±3% accuracy, 
barometric pressure with ±1 hPa absolute accuraccy, 
temperature with ±1.0°C accuracy. B
Because pressure changes with altitude, and the pressure measurements are so good, you can also use it as an altimeter with  ±1 meter or better accuracy!
I2C address 0x77 or 0x76
25.9.- CHF
