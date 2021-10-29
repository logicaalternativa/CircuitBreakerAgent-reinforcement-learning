package com.logicaalternativa.circuitbreakeragent

package report {
  
    sealed trait Metric
    
    case object Load extends Metric
    case object Backend extends Metric
  
} 
