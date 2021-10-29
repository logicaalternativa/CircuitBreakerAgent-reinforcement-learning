package com.logicaalternativa.circuitbreakeragent
package circuitbreaker

import agent._
import scala.concurrent.duration._

trait Environment {
  
  def calculateReward(current: Duration, action: Action[Duration], ratio: Double, isSuccessfull: Boolean): Reward
  
}

object SimpleEnvironment {
  
  def apply(): Environment = new SimpleEnvironment
  
}

class SimpleEnvironment() extends Environment {
  
  def calculateReward(current: Duration, action: Action[Duration], ratio: Double, isSuccessfull: Boolean ): Reward = {
    
    ( current, isSuccessfull ) match {
      case ( Duration.Zero,_ ) => Reward( -1 )
      case ( _, true ) => Reward( ratio / current.toMillis  )
      case ( _, false ) => Reward(0)
    } 
    
    
  }
  
}
