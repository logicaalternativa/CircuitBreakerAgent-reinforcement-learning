package com.logicaalternativa.circuitbreakeragent
package simulator

import clock._
import circuitbreaker._
import report._
import scala.util._
import scala.concurrent._
import scala.concurrent.duration._
import java.time.Instant



object LoadSimulator {
  
  def apply(f: Int => Int)( implicit connector: Connector[DtoIn, Try, DtoOut], serviceReport: LogServiceReport,signalReport: LogSignalReport, ec: ExecutionContext) = new LoadSimulator( f )
  
}


class LoadSimulator(f: Int => Int)( implicit connector: Connector[DtoIn, Try, DtoOut], serviceReport: LogServiceReport, signalReport: LogSignalReport, ec: ExecutionContext ) extends Pulse {
  
  private def sendRequest( timestamp: Timestamp ) : Future[Unit] = {
    
    
    val dtoIn = DtoIn( timestamp )
      
    Future { 
      
      val before = Duration.create( System.currentTimeMillis, "millis" )
    
      val result = connector( dtoIn  ) 
      
      val after = Duration.create( System.currentTimeMillis, "millis" )
          
      serviceReport.log( Load, dtoIn, result, after - before  )
      
      //~ result match {
          //~ case Failure(ConnectorLogger.ExceptionNotPulse(_)) => System.exit(-1)
          //~ case other => ()
      //~ }
   
    } 
      
  }
  
  def pulse( timestamp: Timestamp ): Unit = {
    
     def _pulse( cont: Int ): Unit = {
       
        if( cont > 0) {
            
            sendRequest( timestamp ) 
            
            _pulse( cont - 1 )
          
        }
       
      }
      
      val res = f( timestamp.millis )
      
      signalReport.logSignal( Load, timestamp, res )
    
      _pulse(  res )
      
  }
  
}
