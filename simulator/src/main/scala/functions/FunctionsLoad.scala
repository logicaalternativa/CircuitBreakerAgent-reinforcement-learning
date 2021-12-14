package com.logicaalternativa.circuitbreakeragent
package functions

import Types._

case class VelocityConst( const: Int ) extends FunctionLoad {
  
  def apply(x: Int): Int = const
  
  def isDefinedAt( x: Int ) = x >= 0
  
}

case class Pulse( from: FunctionLoad, to: FunctionLoad, stepSize: Int ) extends FunctionLoad {
  
   def apply(x: Int): Int = {
      
      if ( x % stepSize < stepSize / 2 ) {
          
        to( x )
        
      } else {
        
        from( x )
      }
  
    }
    
    def isDefinedAt( x: Int ) = x >= 0
  
}


case class Poisson( mean: Int ) extends FunctionLoad {
  
  import scala.util._ 
  
  def apply(x: Int): Int = {
    
    def _getCounter( cont: Int, acc: Double ) : Int = {
      
      val interval : Double = -1D * Math.log( Random.nextDouble() ) / mean.toDouble
      
      val newAcc = acc + interval
      
      if ( interval > 1 || newAcc > 1  ) {
        
        cont
                
      } else {
        
          _getCounter( cont +1, newAcc )
      }
    
    }
    
    _getCounter( 0, 0 )
    
    
  }
  
  def isDefinedAt( x: Int ) = x >= 0
  
}


case class AccelationConst( gradient: Float, rest: Float ) extends FunctionLoad {
  
  def apply(x: Int): Int = ( gradient * x + rest ).toInt
  
  def isDefinedAt( x: Int ) = x >= 0
  
}

