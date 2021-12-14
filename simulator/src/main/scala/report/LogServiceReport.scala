package com.logicaalternativa.circuitbreakeragent
package report 

import simulator._
import scala.concurrent.duration._
import scala.util._
import mother.clock._
import mother.data._


trait LogServiceReport {
  
  def log( metric: Metric, dtoIn: DtoIn, dtoOutT: Try[DtoOut], duration: Duration ): Unit
  
}

trait LogSignalReport {
  
  def logSignal( metric: Metric, timestamp: Timestamp, res: Int ): Unit
  
}

trait LogReport extends LogServiceReport with LogSignalReport
  

object LogServiceReportImpl {
  
  import org.slf4j._
  import java.util.UUID
  
  val LoggerServiceLoad = LoggerFactory.getLogger("Load");
  val LoggerServiceBackend = LoggerFactory.getLogger("Backend");
  
  val LoggerSignals = LoggerFactory.getLogger("Signals");
  
  val ReportId = UUID.randomUUID.toString
  
  def apply(): LogReport = new LogServiceReportImpl()
  
  
}

class LogServiceReportImpl extends LogReport {
   
  import LogServiceReportImpl.{LoggerServiceBackend, LoggerServiceLoad, LoggerSignals, ReportId}
  import Metric._

  
  private def createMessage(dtoIn: DtoIn, dtoOutT: Try[DtoOut], duration : Duration) : Option[String]  = dtoOutT match {
    
    case Success( DtoOut( DtoIn(timestamp, id), idOut) ) =>  Some( s""""${ReportId}",${timestamp.millis},${id.value},"OK",${duration.toMillis}""" )
    case Failure( ExceptionDtoOut( DtoIn(timestamp, id), idOut) ) => Some( s""""${ReportId}",${timestamp.millis},${id.value},"ERROR_CB",${duration.toMillis}""" )
    case Failure( e ) =>  Some( s""""${ReportId}",${dtoIn.timestamp.millis},${dtoIn.id.value},"${e.getClass.getSimpleName}",${duration.toMillis}""" )
    
  }
  
  def log( metric: Metric, dtoIn: DtoIn, dtoOutT: Try[DtoOut], duration: Duration ): Unit = {
    
    val message = createMessage( dtoIn, dtoOutT, duration )
    
    (metric, message ) match {
      case (Load , Some(msg) ) => LoggerServiceLoad.info( msg )
      case (Backend, Some(msg) ) => LoggerServiceBackend.info( msg )
      case ( _, _ )             => ()
    }
  }
  
  def logSignal( metric: Metric, timestamp: Timestamp, res: Int ): Unit =  {
      
      LoggerSignals.info(s"${metric},${timestamp.millis},${res}")
  }
  
  
  
}
