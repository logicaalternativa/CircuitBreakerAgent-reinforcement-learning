package com.logicaalternativa.circuitbreakeragent

package circuitbreaker {
  
  import scala.concurrent.duration._
  import scala.util._
  
  import data._
  
  
  sealed trait MaxTime { val value: Duration }
  
  object MaxTime extends SmartConstructor[Duration, MaxTime] {
   
    val error = "The duration should be greater than zero"
    
    def validation( value: Duration ) = value > Duration.Zero
    
    def instance( _value: Duration ) = new MaxTime{ val value = _value } 
    
  }
  
  sealed trait NumberStates { val value: Int }
  
  object NumberStates extends SmartConstructor[Int, NumberStates] {
    
    val error = "The value should be greater than 0"
    
    def validation( value: Int ) = value > 0
   
    def instance( _value: Int ) = new NumberStates{ val value = _value } 
    
  }
  
  case class ConfigQLearning( maxTime: MaxTime, numberStates: NumberStates )
  
  case class ConfigCircuitBreaker( numberOfErrors: Int, timeout: Duration )
  
  sealed trait CircuitBreakerState
  
  case object Closed extends CircuitBreakerState
  case object Open extends CircuitBreakerState
  case object HalfOpen extends CircuitBreakerState
  
  sealed trait CircuitBreakerActions 
  
  case object Ok extends CircuitBreakerActions
  case object Ko extends CircuitBreakerActions
  case object FailFast extends CircuitBreakerActions
  case object AttemptReset extends CircuitBreakerActions
  
  object StateMachine {
    def apply ( current: CircuitBreakerState ): CircuitBreakerActions => Try[CircuitBreakerState] = {
      
        action => {
          
          ( current, action ) match {
            case ( Closed | HalfOpen, Ok            ) => Success( Closed   )
            case ( Closed | HalfOpen ,Ko            ) => Success( Open     )
            case ( Open              , FailFast     ) => Success( Open     )   
            case ( Open              , AttemptReset ) => Success( HalfOpen )
            case ( state             , action       ) => Failure( new IllegalArgumentException( s"State change not be allowed: ${state} -> ${action}" ) ) 
          }
          
        }
      
    }
  } 
}
