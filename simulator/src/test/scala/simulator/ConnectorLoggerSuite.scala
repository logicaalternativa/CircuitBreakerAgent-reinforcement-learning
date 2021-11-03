package com.logicaalternativa.circuitbreakeragent
package simulator

import org.scalatest.funsuite.AnyFunSuite

import clock._
import scala.concurrent._
import scala.util._
import report._

import mother.clock._
import mother.data._
import mother.randomInt
    

object ConnectorLoggerSuite {
  
  import functions._  
  
  implicit val ec: ExecutionContext = ExecutionContext.global
  
  implicit val period : Period = Period( millis = 200 )
  
  def reportInputSimulatorFactory = Mother.reportSimulatorFactory( Backend ) _
  
  def signalReportFactory = Mother.signalReportFactory( Backend ) _
  
  val numberCalls = randomInt
    
  val f = VelocityConst( numberCalls ) 
  
  val expectedTs = Timestamp( randomInt )
    
    
  
}


class ConnectorLoggerSuite extends AnyFunSuite {
  
  import ConnectorLoggerSuite._
  import scala.concurrent.duration._

  
  test("Happy path, It should allowed every call") {
    
    val expectedCalls = f( expectedTs.millis )
    
    val reportSimulator = reportInputSimulatorFactory( expectedTs, expectedCalls )
    
    val signalReport = signalReportFactory( expectedTs, expectedCalls )
    
    val connector = ConnectorLogger( f )(  period, reportSimulator, signalReport )
    
    connector.pulse( expectedTs )
    
    val res =  Future {
      for (_ <- 0 until expectedCalls ) 
        connector( DtoIn( expectedTs ) )
    }
    
    assert( Try( Await.result( res, 1.second ) ) == Success( () ) , "Every call should be accepted" )
    
    assert( reportSimulator.calls.size == expectedCalls, "Every reg is saved"  )
    assert( signalReport.calls.size == 1, "Repor signal is called only once"  )
    
  }
  
  test("The last call should be failed") {
    
    val expectedCalls = f( expectedTs.millis ) + 1
    
    val reportSimulator = reportInputSimulatorFactory( expectedTs, expectedCalls )
        
    val signalReport = signalReportFactory( expectedTs, expectedCalls - 1 )
    
    val connector = ConnectorLogger( f )(  period, reportSimulator, signalReport )
    
    connector.pulse( expectedTs )
    
    val res =  Future( 
      for( reg <- 0 until expectedCalls ) 
        connector( DtoIn( expectedTs ) ).get
    )
    
    val resT = Try( Await.result( res, 600.millis ) )
     
    assert( resT.isFailure , s"The last call should be waiting ${resT}" )
      
    assert( reportSimulator.calls.size >= expectedCalls -1, "Every reg is saved" )
    
    assert( signalReport.calls.size == 1, "Repor signal is called only once"  )
    
  }
  
  
  
} 
