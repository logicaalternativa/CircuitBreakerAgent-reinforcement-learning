package com.logicaalternativa.circuitbreakeragent
package clock

trait Pulse {
  
  def pulse( timestamp: Timestamp ): Unit
  
} 
