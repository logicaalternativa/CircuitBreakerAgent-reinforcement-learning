package com.logicaalternativa.circuitbreakeragent
package mother 

import scala.concurrent._
    
class NumberCalls[T] {
  
  protected val promise= Promise[Unit]()
  
  protected var reg: List[T] = List()
  
  protected def add( value: T ): Unit = this.synchronized {
     
    reg = reg :+ value 
    
  }
  
  protected def checkNumberOfCalls( expectedNumberCalls: Int )  = {
          
      if ( calls.size == expectedNumberCalls ) {
          promise.success( () )
      }
    
  }
  
  def calls: List[T] = reg
  
  def future = promise.future
  
}
