#!/usr/bin/env bash
### BEGIN INIT INFO
# Provides:          iot-scala
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

export CONFIGURATION_FILE="/home/pi/iot-scala/configuration.properties"
case "$1" in
  start)
    echo "Starting iot-scala"
    java -jar /home/pi/iot-scala/iot-scala*.jar &> /home/pi/iot-scala/app.log &
    ;;
  stop)
    echo "Stopping iot-scala"
    pkill -f ".*iot-scala.*.jar";
    ;;
  restart)
    $0 stop
    sleep 0.5
    $0 start
    ;;
  status)
    pgrep -f ".*iot-scala.*.jar" &> /dev/null;
    exit $?
    ;;
  *)
    echo "Usage: /etc/init.d/iot-scala {start|stop|restart|status}"
    exit 1
    ;;
esac

exit 0;
