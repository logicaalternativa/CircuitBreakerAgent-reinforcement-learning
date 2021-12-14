package com.logicaalternativa.circuitbreakeragent

import scala.util._

import data._

package agent {
   

  case class Reward( value: Double ) 
   
  sealed trait Epsilon { val value: Double }
  
  trait SmartConstructorDouble[T] extends SmartConstructor[Double, T] {
     
      val error = "Value have to between 0 and 1" 
      def validation( value: Double ) = value >= 0 && value <= 1.0
     
   }
   
  object Epsilon  {
    
    private val smartConstructor = new SmartConstructorDouble[Epsilon] {
      
      def instance( _value: Double ) = new Epsilon{ val value = _value } 
      
    }
    
    def apply( value: Double ) = smartConstructor( value )
 
  }
  
  
  sealed trait DiscountRate { val value: Double }
   
  object DiscountRate {
    
    private val smartConstructor = new SmartConstructorDouble[DiscountRate] {
      
      def instance( _value: Double ) = new DiscountRate{ val value = _value } 
      
    }
    
    def apply( value: Double ) = smartConstructor( value )
 
  }
  
  sealed trait StepSize { val value: Double }
   
  object StepSize extends SmartConstructorDouble[StepSize] {
     
   def instance( _value: Double ) = new StepSize{ val value = _value } 
 
  }
  
 
 case class Q[State]( state: State, action: Action[State], estimate: Double  )
   
}


