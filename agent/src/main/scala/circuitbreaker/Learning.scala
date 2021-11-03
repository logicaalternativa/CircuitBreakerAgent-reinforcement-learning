package com.logicaalternativa.circuitbreakeragent
package circuitbreaker

import agent._
import scala.concurrent.duration._
import scala.util._

trait Learning {
  
  def initHalfOpenDuration: Duration
  
  def nextHalfOpenDuration( current: Duration ): Action[Duration] 
  
  def publish( current: Duration, action:Action[Duration], ratio: Double, isSuccesfull: Boolean ): Try[Unit]
  
}

object StandardLearning {
   
   def apply( timeoutHalfOpen: Duration ): Learning = new StandardLearning( timeoutHalfOpen )
   
}

class StandardLearning( timeoutHalfOpen : Duration ) extends Learning {
   
  def initHalfOpenDuration: Duration = timeoutHalfOpen
  
  def nextHalfOpenDuration( current: Duration ): Action[Duration] = new Action[Duration] {
     
     val id: String = s"fiexedDuration-${timeoutHalfOpen}"
     
     def apply(v1: Duration): Duration = timeoutHalfOpen
     
   }
  
  def publish( current: Duration, action:Action[Duration], ratio: Double, isSuccesfull: Boolean )  = Success( () )
   
   
}
