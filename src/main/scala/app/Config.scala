package com.logicaalternativa.circuitbreakeragent
package app

import clock._



object Config {
    
  import report._
  import simulator._
  import functions._
  import circuitbreaker._
  import agent._
  import scala.util._
  import scala.concurrent._
  import scala.concurrent.duration._
  import java.util.concurrent._
  
  
  // Load
  val functionLoad = Poisson( 2 ) 
  val functionLoadConnector = Poisson( 2 )
  
  // Clock
  val period = Period( 75 )
  val numberOfPulses = NumberPulses( 30000 )
  
  // Circuirbreaker  
  val numberOfErrors = 3
  val timeoutExec = 220.millis
  
  
  // Qlearning  
  val ennabledQlearning = false
  
    val stepSize = StepSize(0.8).get
    val discountRate = DiscountRate( 0.5 ).get
    val epsilon = Epsilon(0.15).get
    
    val maxTime = MaxTime( 6.second ).get
    val numberStates = NumberStates( 6 ).get
  
  // Standar learning
  val durationStandarLearning = 500.millis
  
  
  
}
