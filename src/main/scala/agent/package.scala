package com.logicaalternativa.circuitbreakeragent

import scala.util._

package agent {
   
  import data._

  case class Reward( value: Double ) 
   
  sealed trait Epsilon { val value: Double }
  
  trait SmartConstructorDouble[T] extends SmartConstructor[Double, T] {
     
      val error = "Value have to beteween 0 and 1" 
      def validation( value: Double ) = value >= 0 && value <= 1.0
     
   }
   
  object Epsilon extends SmartConstructorDouble[Epsilon] {
     
   def instance( _value: Double ) = new Epsilon{ val value = _value } 
 
  }
  
  
  sealed trait DiscountRate { val value: Double }
   
  object DiscountRate extends SmartConstructorDouble[DiscountRate] {
     
   def instance( _value: Double ) = new DiscountRate{ val value = _value } 
 
  }
  
  sealed trait StepSize { val value: Double }
   
  object StepSize extends SmartConstructorDouble[StepSize] {
     
   def instance( _value: Double ) = new StepSize{ val value = _value } 
 
  }
  
 
 case class Q[State]( state: State, action: Action[State], estimate: Double  )
   
}


