package com.logicaalternativa.circuitbreakeragent
package mother

package data{
  
    import clock._
    import java.time.Instant
  
    case class Uuid( value: String = System.nanoTime.toString )
  
    case class DtoIn( timestamp: Timestamp, id: Uuid = Uuid() ) {
      val created = Instant.now
    } 
    
    case class DtoOut( fromIn: DtoIn, id: Uuid = Uuid() ){
      val created = Instant.now
    }
  
}
