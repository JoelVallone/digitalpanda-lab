package org.digitalpanda.iot.raspberrypi.components

import com.pi4j.io.gpio.{GpioFactory, Pin, PinState}
import org.digitalpanda.iot.raspberrypi.components.DiodeState._

object DiodeState {
  sealed trait DiodeState
  case object On extends DiodeState
  case object Off extends DiodeState
  case object Blink extends DiodeState
}

class Diode(val gpioId: Pin, val pinName: String) {

  private val gpioPin = GpioFactory.getInstance.provisionDigitalOutputPin(gpioId, pinName, PinState.LOW)

  private var currentState: DiodeState = Off
  private var targetState: DiodeState = Off

  def targetBlink(): Unit = {
    targetState = Blink
  }

  def targetEnabled(): Unit = {
    targetState = On
  }

  def targetDisabled(): Unit = {
    targetState = Off
  }

  def updateVoltage(): Unit = {
    if (currentState != targetState) {
      targetState match {
        case On     => gpioPin.setState(true)
        case Off    => gpioPin.setState(false)
        case _      => // Nop
      }
    } else {
      targetState match {
        case Blink  => gpioPin.toggle()
        case _      => // Nop
      }
    }
    currentState = targetState
  }
}
