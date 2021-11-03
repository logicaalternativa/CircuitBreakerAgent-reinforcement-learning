package com.logicaalternativa.circuitbreakeragent
package data

import scala.util._

trait SmartConstructor[V, T] {
    
  val error: String
  
  def validation( v: V ): Boolean
  
  def instance( value: V ): T
  
  def apply( _value: V ): Try[T]= {
    if ( ! validation( _value )   ) {
        Failure( new IllegalArgumentException( error ) )
    } else  {
        Success( instance( _value ) )
    }
  }
    
 }
