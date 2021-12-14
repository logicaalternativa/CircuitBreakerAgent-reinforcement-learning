package com.logicaalternativa.circuitbreakeragent
package simulator

import mother.clock.{Timestamp, Period}
import circuitbreaker._
import scala.util._
import scala.concurrent.duration._
import functions._
import Types.FunctionLoad
import report._
import mother.clock._
import mother.data._
import clock.Pulse


object ConnectorLogger {
  
  import java.time.Instant  
  
  def apply( f: FunctionLoad )( using period: Period, serviceReport: LogServiceReport, signalReport: LogSignalReport ): Pulse with Connector[DtoIn, Try, DtoOut] = new ConnectorLogger( f )
  
  case class ExceptionNotPulse( dtoIn: DtoIn ) extends Throwable {
  
      override def getMessage() = s"Error by pulse update, dto ${dtoIn}"
    
  }
  
  def currentTime = Instant.now.toEpochMilli
  
}


class ConnectorLogger( f: FunctionLoad  )( using period: Period, serviceReport: LogServiceReport, signalReport: LogSignalReport) extends Pulse with Connector[DtoIn, Try, DtoOut]  {
  
  import java.util.concurrent.atomic._
  import scala.annotation.tailrec
  import ConnectorLogger._
  import scala.util.Random
  import Metric.Backend
  
  val atomicInt = new AtomicInteger(0)
  
  var lastUpdate = currentTime
  
  
  def pulse( timestamp: Timestamp ): Unit = {
    
    lastUpdate = currentTime
    
    val res = f( timestamp.millis )
    
    signalReport.logSignal( Backend, timestamp, res ) 
    
    atomicInt.set( res )
  }
  
  // TODO Latency should be configured
  def simulateLatency: Unit = Thread.sleep( Random.nextInt( 20 ) + 20 )
  
  def apply(dtoIn: DtoIn): Try[DtoOut] = {
    
    @tailrec 
    def _apply(dtoIn: DtoIn, before: Duration): Try[DtoOut] = {
      
      val count = atomicInt.decrementAndGet()
          
      if ( count >= 0 ) {        
        
        val result = Success( DtoOut( fromIn = dtoIn ) )
        
        val after = Duration.fromNanos( currentTime )
        
        simulateLatency
        
        serviceReport.log( Backend, dtoIn, result, after - before  )
        
        result
        
      } else if ( currentTime - lastUpdate > 2* period.millis ){
          
        Failure( ExceptionNotPulse( dtoIn ) )
        
        
      } else {
        
        Thread.sleep( Math.abs( lastUpdate + period.millis - currentTime ) ) 
        
        _apply( dtoIn, before )        

      } 
      
    }
    
    val before = Duration.fromNanos( currentTime )
    
    _apply( dtoIn, before )
    
  }
  
  
}
