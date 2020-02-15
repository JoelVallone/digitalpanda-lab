package org.digitalpanda.iot.actors.measure.source

import akka.actor.{Actor, ActorLogging, ActorRef, Timers}
import org.digitalpanda.iot.MeasureType.MeasureType
import org.digitalpanda.iot.{Location, NewMeasures}

import scala.concurrent.duration.FiniteDuration


abstract class SourceActor(samplingPeriod: FiniteDuration,
                           targetMeasures : List[(Location, MeasureType)],
                           consumer: ActorRef) extends Actor with ActorLogging with Timers {

  timers.startTimerWithFixedDelay(MeasurePush, PushNewMeasure, samplingPeriod)

  override def receive: Receive = {
    case PushNewMeasure => consumer ! sampleNewMeasures(nextMeasureId())
    case message => log.warning(s"A measure source actor should not receive any external message.\nReceived: $message")
  }

  def sampleNewMeasures(requestId: Long): NewMeasures


  private case object MeasurePush
  private case object PushNewMeasure

  private var requestId: Long = 0
  private def nextMeasureId() : Long = {
    requestId += 1
    requestId
  }
}
