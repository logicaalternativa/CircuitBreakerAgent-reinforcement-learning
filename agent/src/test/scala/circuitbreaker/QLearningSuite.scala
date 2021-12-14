package com.logicaalternativa.circuitbreakeragent
package circuitbreaker

import org.scalatest.funsuite.AnyFunSuite

import scala.util._
import agent._
import scala.concurrent.duration._



object QLearningSuite {
  
  
}


class QLearningSuite extends AnyFunSuite {
  
  import QLearningSuite._
  
  test("") {
    
    val configQLearning = ConfigQLearning( MaxTime( 10.second ).get, NumberStates(10).get )

    val stepSize = StepSize(1).get
    val discountRate = DiscountRate( 1 ).get
    val epsilon = Epsilon(0).get
    val environment = SimpleEnvironment()

    val qLearning = QLearning( configQLearning, stepSize, discountRate, epsilon, environment )

    val initState = qLearning.initHalfOpenDuration

    //~ println( s" kkkkkkkkkkkkkkkkkkkkk initState  ${initState}" )

    val action = qLearning.nextHalfOpenDuration( initState )

    //~ println( s" kkkkkkkkkkkkkkkkkkkkk action  ${action}" )

    qLearning.publish( initState, action, 0.5, true )
  
    
  }
  
    
} 
