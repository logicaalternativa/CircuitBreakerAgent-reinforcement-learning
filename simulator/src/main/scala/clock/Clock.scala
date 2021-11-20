package com.logicaalternativa.circuitbreakeragent
package clock

import scala.concurrent._
import scala.annotation.tailrec

import mother.clock._


object Clock {
  
    def apply( implicit period: Period, numberPulses: NumberPulses, listPulse: List[Pulse], executionContex: ExecutionContext ) : Clock= new ClockImp
}

trait Clock{
  
    def start: Unit
    def stop: Unit
    def isRunning: Boolean
    
}


private class ClockImp( implicit period: Period, numberPulses: NumberPulses, listPulse: List[Pulse], executionContex: ExecutionContext ) extends Clock {
  
   import Thread.sleep
   import org.slf4j._
   
   val logger = LoggerFactory.getLogger("com.logicaalternativa.circuitbreakeragent.clock.ClockImp");
 
   var started = true
   
   def executePulse( timestamp: Timestamp ) : Future[List[Unit]] =  Future.sequence { 
      listPulse.map{
        s => Future( s.pulse( timestamp ) ) 
      }
   }
  
   private def _start( count: Int ) : Unit = {
      if ( count >= numberPulses.value ) {
         logger.info( "Clock stopped by number of pulses") 
         stop
      } else if ( isRunning ) {
        val timestamp = Timestamp( count * period.millis )
        executePulse( timestamp ).map { _ => 
            sleep( period.millis )
            _start( count + 1 )
        }
      } else {
         logger.info( "Clock stopped")
      }
   }
  
  
   override def start =  _start(0)
   
   override def stop  = {
      started = false
    }
    
   override def isRunning = started
  
} 
