package com.logicaalternativa.circuitbreakeragent

package mother

package clock {
  
  case class Timestamp( millis: Int ) extends AnyVal
  case class Period( millis: Int ) extends AnyVal
  case class NumberPulses( value: Int) extends AnyVal
  
}
