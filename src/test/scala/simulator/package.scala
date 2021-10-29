package com.logicaalternativa.circuitbreakeragent

import clock._
import report._
import test._
import scala.concurrent._
import scala.concurrent.duration._
import scala.util._


package simulator {

  object Mother {
    
    def reportSimulatorFactory( expectedMet: Metric )( expectedTs: Timestamp, expectedNumberCalls: Int ) = new NumberCalls[Unit] with LogServiceReport {
       def log(metric: Metric, dtoIn: DtoIn, dtoOutT: Try[DtoOut], duration: Duration): Unit = {
         
         ( metric, dtoIn, dtoOutT ) match {
              case ( met, dtoIn @DtoIn( ts, _ ), Success( DtoOut( fromDtoIn: DtoIn, _  ) ) )  if ( met ==  expectedMet && ts == expectedTs && dtoIn == fromDtoIn ) => {
                  add( () )
                  checkNumberOfCalls( expectedNumberCalls )
              }
              case (_,_,_) => ()
          } 
        }
    }
    
    def signalReportFactory( expectedMetric: Metric)(expectedTs: Timestamp, expectedNumberCalls: Int )  = new NumberCalls[Unit] with LogSignalReport {
    
      def logSignal( metric: Metric, timestamp: Timestamp, res: Int ): Unit = {
        
          if ( metric == expectedMetric && timestamp == expectedTs && res == expectedNumberCalls ) {
            add( () )
            checkNumberOfCalls( 1 )
            
          } 
      }
      
    } 
    
  }

} 

