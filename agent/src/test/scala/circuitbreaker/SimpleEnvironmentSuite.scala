package com.logicaalternativa.circuitbreakeragent
package circuitbreaker

import org.scalatest.funsuite.AnyFunSuite

import scala.util._
import agent._
import scala.concurrent.duration._



object SimpleEnvironmentSuite {

  val time = Random.nextInt( 20 ) + 1
    
  val ratio = Random.nextDouble()
  
  val expectedDuration = time.second
    
  
}


class SimpleEnvironmentSuite extends AnyFunSuite {
  
  import SimpleEnvironmentSuite._
  
  test("If the isSuccesfull is true should return the inverse of the duration state multiplying by the ratio") {
    
    val currentReward = SimpleEnvironment().calculateReward( current = expectedDuration, action = Action( _ => ??? ), ratio = ratio, true)
    
    assert( ratio / expectedDuration.toMillis == currentReward.value, "The reward should be the inverse of the currrent duration in seconds" )
    
  }
  
  
  test("If the isSuccesfull is false should return reward 0") {
    
    
    val currentReward = SimpleEnvironment().calculateReward( current = expectedDuration, action = Action( _ => ??? ), ratio = ratio, false)
    
    assert( 0 == currentReward.value, "The reward should be the inverse of the currrent duration in seconds (value minus zero)" )
    
    
  }
  
  test("If the isSuccesfull is true and current duration is Zero the reward should be -1") {
    
    val currentReward = SimpleEnvironment().calculateReward( current = Duration.Zero, action = Action( _ => ??? ), ratio = Random.nextDouble(), true)
    
    assert( -1 == currentReward.value, "The reward should be the minus one" )
    
    
  }
  
  
  
    
  
    
} 
