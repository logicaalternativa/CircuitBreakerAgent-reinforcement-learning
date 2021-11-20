package com.logicaalternativa.circuitbreakeragent
package app

import mother.data._
import clock.Clock

object Run extends App {
    
  import report._
  import simulator._
  import circuitbreaker._
  import agent._
  import scala.util._
  import scala.concurrent._
  import scala.concurrent.duration._
  import java.util.concurrent._
  
  import Config._
  
  private def createCircuitBreaker( decored: Connector[DtoIn,Try, DtoOut] ): Connector[DtoIn,Try, DtoOut] = {
    
      
    val learning = if ( ennabledQlearning ) {
   
      val configQLearning = ConfigQLearning( maxTime, numberStates  )
      
      val environment = SimpleEnvironment()
      
      QLearning( configQLearning, stepSize, discountRate, epsilon, environment )
    
    } else {
      
       StandardLearning( durationStandarLearning )
      
    }
    
    
    val errorOpenState: DtoIn => Throwable = ExceptionDtoOut( _ )
    
    val ecCircuitBreaker = ExecutionContext.fromExecutor( Executors.newFixedThreadPool(4) )  
    
    val configCB = ConfigCircuitBreaker( numberOfErrors = numberOfErrors, timeoutExec )
 
    CircuitBreakerSync( connector = decored, config = configCB , errorOpenState = errorOpenState, learning = learning)( ec = ecCircuitBreaker )
    
  }
  
  val serviceReport: LogReport = LogServiceReportImpl()  
  
  val connectorLogger = ConnectorLogger( f = functionLoadConnector )( period = period, serviceReport = serviceReport, signalReport = serviceReport )
  
  val circuitBreaker: Connector[DtoIn,Try, DtoOut] = createCircuitBreaker( connectorLogger )
  
  val ecLoadSimulator = ExecutionContext.fromExecutor( Executors.newFixedThreadPool( 4 ) )  
  
  val loadSimulator = LoadSimulator( f= functionLoad )( connector = circuitBreaker, serviceReport = serviceReport, signalReport = serviceReport, ec = ecLoadSimulator )
  
  val ecClock = ExecutionContext.fromExecutor( Executors.newFixedThreadPool(4) ) 
  
  val clock = Clock( period = period, numberPulses = numberOfPulses, listPulse = List( connectorLogger, loadSimulator  ), executionContex = ecClock ) 
  
  clock.start
  
}
