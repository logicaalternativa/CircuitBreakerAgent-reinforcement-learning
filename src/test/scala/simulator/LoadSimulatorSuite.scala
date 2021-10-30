package com.logicaalternativa.circuitbreakeragent
package simulator

import org.scalatest.funsuite.AnyFunSuite

import test.NumberCalls
import clock._
import functions._
import circuitbreaker._
import report._
import scala.util._
import scala.concurrent._

import scala.concurrent.duration._
import scala.language.postfixOps

object LoadSimulatorSuite {
  
  def connectorSimulatorMockFactory( expectedTs: Timestamp, expectedNumberCalls: Int )  = new NumberCalls[DtoIn] with Connector[DtoIn, Try, DtoOut]  {
    
    def apply( dto: DtoIn ) = {
      if ( dto.timestamp == expectedTs ) {
          add( dto )
          checkNumberOfCalls( expectedNumberCalls )
          Success( DtoOut( fromIn = dto ) )
          
      } else {
        Failure( new RuntimeException( "Error dtoIn is not expected" ) )
      }
      
    }
    
  }
  
  def signalReportFactory = Mother.signalReportFactory( Load ) _
  
  def reportInputSimulatorFactory = Mother.reportSimulatorFactory( Load ) _
  
  val f = AccelationConst(gradient = Random.nextFloat() * Random.nextInt(10), rest = Random.nextFloat() * Random.nextInt(10))
  
  implicit val ec: ExecutionContext = ExecutionContext.global
  
}


class LoadSimulatorSuite extends AnyFunSuite {
  
  import LoadSimulatorSuite._
  import clock._  
  
  import scala.concurrent._
    
  
  test("The values acumulates should the same of the queue") {
    
    val expectedTimestamp = Timestamp( Random.nextInt(100) )
    
    val expectedNumberCalls = f( expectedTimestamp.millis )
    
    
    val reportSimulator = reportInputSimulatorFactory( expectedTimestamp, expectedNumberCalls )
    
    val connectorSimulator = connectorSimulatorMockFactory( expectedTimestamp, expectedNumberCalls )
    
    val signalReport = signalReportFactory( expectedTimestamp, expectedNumberCalls )
    
    LoadSimulator( f )( connectorSimulator, reportSimulator, signalReport, ec ).pulse( expectedTimestamp )
    
    val res = for {
      
       _ <- connectorSimulator.future 
       _ <- reportSimulator.future  
       _ <- signalReport.future  
      
    }yield ()
    
    Await.ready( res, 10 seconds )
    
    assert( connectorSimulator.calls.filter( _.timestamp == expectedTimestamp ).size == expectedNumberCalls, "Every value has the same timestamp and the calls should be the same of the function"  )
    
    assert( reportSimulator.calls.size == expectedNumberCalls, "Every reg is saved"  )
    assert( signalReport.calls.size == 1, "The signal is registered only one time"  )
    
  }
  
  
  test("The values returns the function less than zero should not enqueue") {
    
    
    val expectedTimestamp = Timestamp( Random.nextInt(100) )
    
    val expectedNumberCalls = 0
    
    val connectorSimulator = connectorSimulatorMockFactory( expectedTimestamp, expectedNumberCalls )
    
    val reportSimulator = reportInputSimulatorFactory( expectedTimestamp, expectedNumberCalls )
    val signalReport = signalReportFactory( expectedTimestamp, expectedNumberCalls )
    
   
    LoadSimulator( VelocityConst(-1) )( connectorSimulator, reportSimulator, signalReport, ec ).pulse( expectedTimestamp ) 
    
      val res = for {
      
       _ <- connectorSimulator.future 
       _ <- reportSimulator.future  
       _ <- signalReport.future  
      
    }yield ()
    
    Try{ 
      Await.ready( connectorSimulator.future flatMap { _ => reportSimulator.future }, 1 seconds ) 
    } 
    
    assert( connectorSimulator.calls.size == expectedNumberCalls, "No enqueue any value"  )
    
    assert( reportSimulator.calls.size == expectedNumberCalls, "Nothing is saved"  )
    assert( signalReport.calls.size == 0, "No signal is registered"  )
    
  }
  
} 
