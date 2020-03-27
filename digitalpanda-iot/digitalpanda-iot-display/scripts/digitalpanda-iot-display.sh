#!/usr/bin/env bash
### BEGIN INIT INFO
# Provides:          digitalpanda-iot-display
# Required-Start:    $remote_fs $syslog
# Required-Stop:     $remote_fs $syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Start daemon at boot time
# Description:       Enable service provided by daemon.
### END INIT INFO


# update boot / reboot files
#cp <your script> /etc/init.d/
# do it as soon as the device is going down,
#   both for shutdown and reboot
# update-rc.d <your script> defaults

export CONFIGURATION_FILE="/home/pi/digitalpanda-iot-display/configuration.properties"
case "$1" in
  start)
    echo "Starting iot-display"
    java -jar /home/pi/digitalpanda-iot-display/digitalpanda-iot-display*.jar &> /home/pi/digitalpanda-iot-display/app.log &
    ;;
  stop)
    echo "Stopping iot-display"
    pkill -f ".*digitalpanda-iot-display.*.jar";
    ;;
  restart)
    $0 stop
    sleep 0.5
    $0 start
    ;;
  status)
    pgrep -f ".*digitalpanda-iot-display.*.jar" &> /dev/null;
    exit $?
    ;;
  *)
    echo "Usage: /etc/init.d/iot-display {start|stop|restart|status}"
    exit 1
    ;;
esac

exit 0;
