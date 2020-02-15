package org.digitalpanda.iot.raspberrypi.circuits.panel

import com.pi4j.io.gpio.{Pin, RaspiPin}
import org.digitalpanda.iot.raspberrypi.components.Diode

object PanelDisplay {

  def apply(): PanelDisplay =
    new PanelDisplay(RaspiPin.GPIO_00, RaspiPin.GPIO_02)

  def apply(windowRedDiodeId: Pin, windowGreenDiodeId: Pin): PanelDisplay =
    new PanelDisplay(windowRedDiodeId, windowGreenDiodeId)
}

class PanelDisplay (windowRedDiodeId: Pin,
                    windowGreenDiodeId: Pin) {

  private val windowRedDiode = new Diode(windowRedDiodeId, "windowRedDiode")
  private val windowGreenDiode = new Diode(windowGreenDiodeId, "windowGreenDiode")

  def targetPanicWindowState(): Unit = {
    windowGreenDiode.targetDisabled()
    windowRedDiode.targetBlink()
  }

  def targetOpenWindow(canOpenWindow: Boolean): Unit = {
    if (canOpenWindow) {
      windowRedDiode.targetDisabled()
      windowGreenDiode.targetEnabled()
    } else {
      windowRedDiode.targetEnabled()
      windowGreenDiode.targetDisabled()
    }
  }

  def shutdown(): Unit =  {
    windowRedDiode.targetDisabled()
    windowGreenDiode.targetDisabled()
  }

  def applyStateToDisplay(): Unit = {
    windowRedDiode.updateVoltage()
    windowGreenDiode.updateVoltage()
  }

}
