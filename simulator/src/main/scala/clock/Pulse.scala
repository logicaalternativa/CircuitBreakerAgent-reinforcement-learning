package com.logicaalternativa.circuitbreakeragent
package clock

import mother.clock.Timestamp

trait Pulse {
  
  def pulse( timestamp: Timestamp ): Unit
  
} 
