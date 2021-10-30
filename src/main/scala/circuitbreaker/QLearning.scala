package com.logicaalternativa.circuitbreakeragent
package circuitbreaker

import agent._
import scala.concurrent.duration._
import scala.util._


object QLearning {
   
   def apply( configQLearning: ConfigQLearning, stepSize: StepSize, discountRate: DiscountRate, epsilon: Epsilon, environment: Environment ) = {
      
      new QLearning( configQLearning, stepSize, discountRate, epsilon, environment )
       
   }
   
}

class QLearning( private val configQLearning: ConfigQLearning, private val stepSize: StepSize, private val discountRate: DiscountRate, private val epsilon: Epsilon, private val environment: Environment ) extends Learning {
   
  
   val listState: List[Duration] = {
     
      val step: Duration = configQLearning.maxTime.value / configQLearning.numberStates.value
         
      def _createList( l: List[Duration], from: Duration ): List[Duration] = {
         
         if ( from  >= configQLearning.maxTime.value ) {
            
            l
               
         } else {
            
            val state = from + step
            
            _createList( l :+ state, state )
            
         }
         
      }
      
      _createList( Nil, Duration.Zero )
     
  }
  
  
  val qs = {
     
     val listAction = ( listState map { action( _ ) } )
   
     listState flatMap {
     
         st0 => listAction map {
         
            action => Q[Duration]( state = st0, action = action, estimate = 0 )
            
         }
     
      }
  }
  
  private def action( to: Duration ) = new Action[Duration] {
      
      val id = to.toString
      
      override def toString = s"Action($id)"
      
      def apply( other : Duration ) = to
      
  }
  
  
  val agent = QAgent( qs, stepSize, discountRate, epsilon )
  
  def initHalfOpenDuration: Duration = {
      
      val randomIndex = Random.nextInt( listState.size )
      
      listState( randomIndex )
     
  }
  
  def nextHalfOpenDuration( current: Duration ): Action[Duration] = {
     
      agent.next( current )
   }
  
  def publish( current: Duration, action:Action[Duration], ratio: Double, isSuccessfull: Boolean ): Try[Unit] = {
      
      val reward = environment.calculateReward( current, action, ratio, isSuccessfull )
      
      agent.reward( current, action, reward )
     
   }
   
}
