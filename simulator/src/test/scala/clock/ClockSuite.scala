package com.logicaalternativa.circuitbreakeragent
package clock

import org.scalatest.funsuite.AnyFunSuite

import mother.data._
import mother.clock._
import mother.{randomInt, NumberCalls}


object ClockSuite {
  
  val period = Period( millis = 200 )
  
  def pulseFactory = new NumberCalls[Timestamp] with Pulse  {
    
    def pulse( timeStamp: Timestamp ) = {
        
      add( timeStamp )
      
    }    
    
  }
  
}


class ClockSuite extends AnyFunSuite {
  
  import ClockSuite._
  import scala.concurrent._
  import Thread.sleep
  
  
  
  test("The pulse should be send when the configuration is ok") {
    val pulse = pulseFactory
    
    val numberPulses = NumberPulses( 3 )
    
    val clock = Clock( period, numberPulses, List( pulse ), ExecutionContext.global )
    val expected = List( Timestamp( 0 ), Timestamp( period.millis ), Timestamp( 2* period.millis ) )
    clock.start
    sleep( (2.5 * period.millis ).toInt ) 
    clock.stop
    assert(expected == pulse.calls, s"The list of the call should be equals")
  }
  
  
  test("Checking number of pulses") {
    
    val pulse = pulseFactory
    
    val expectedTimes = randomInt
    
    val numberPulses = NumberPulses( expectedTimes )
    
    val clock = Clock( period, numberPulses, List( pulse ), ExecutionContext.global )
    clock.start
    sleep( ( period.millis * (expectedTimes + 2) ).toInt  ) 
    
    assert(! clock.isRunning, s"The list of the call should be equals")
  }
  
} 
