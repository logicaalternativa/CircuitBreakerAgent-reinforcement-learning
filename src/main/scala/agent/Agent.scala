package com.logicaalternativa.circuitbreakeragent
package agent
import scala.util._
import java.util.UUID

object Action {
   
   def apply[State]( f: State => State ): Action[State] = new Action[State] {
      
      val id: String = UUID.randomUUID().toString
      override def apply( s: State ) = f( s )
      override def toString = s"Action(id=$id)"
   }

}

trait Action[State] extends ( State => State ) {
   val id: String
}

trait Agent[State] {
   
   def next( current: State ): Action[State]
   
   def reward( state: State, action: Action[State], reward: Reward ): Try[Unit]
   
}


trait Metrics[State] {
   
   sealed trait Type 
   case object Greedy extends Type
   case object Epsilon extends Type
   
   def metricNext( tp: Type, from: State, to: Action[State]): Unit
   
}

object Metrics {
   
   import org.slf4j._
   
   def default[State]: Metrics[State] = new Metrics[State]  {
      
      private val logger = LoggerFactory.getLogger("QAgent-metrics")
   
      override def metricNext( tp: Type, from: State, to: Action[State]): Unit = {
         
         logger.info( s"${System.currentTimeMillis},${tp},${from},${to}")
         
      }
      
   }
   
}

object QAgent {
   
   import org.slf4j._
   
   val logger = LoggerFactory.getLogger("com.logicaalternativa.circuitbreakeragent.agent.QAgent");
   
   def apply[State]( qs: List[Q[State]], stepSize: StepSize, discountRate: DiscountRate, epsilon: Epsilon, metric: Metrics[State] = Metrics.default[State] ): Agent[State] = new QAgent( qs, stepSize, discountRate, epsilon, metric )
   
   def getRandom[State]( l: List[Q[State]] ): Q[State] = {
      
      val index = Random.nextInt( l.size )
      
      l( index )
      
   }
   
   def filterFromCurrent[State]( l : List[Q[State]], current: State ) = l filter { _.state == current }
   
   def getGreedy[State](l : List[Q[State]], current: State): Q[State] = {
      
      val filtered = filterFromCurrent( l, current )
         
      val max  = filtered reduce { 
         ( one, two ) => if (one.estimate > two.estimate) one else two
      }
      
      val regWihtMaxEstimate = filtered filter {
         _.estimate == max.estimate
      }
      
      getRandom( regWihtMaxEstimate  )
      
   }
 
}

class QAgent[State]( private val qs: List[Q[State]], private val stepSize: StepSize, private val discountRate: DiscountRate, private val epsilon: Epsilon, metric: Metrics[State] ) extends Agent[State] {
   
   import QAgent._
   
   private var internalState: List[Q[State]] = qs
   
   private def maxNextValue( current: Q[State] ): Double = {
      
      val nextState = current.action( current.state )
      
      getGreedy ( internalState, nextState ).estimate
      
   }
   
   private def calculate(current: Q[State], reward: Reward ) : Q[State] = {
      
      // Q(S, A) ← Q(S, A) + α[R + γ maxa Q(S' , a) − Q(S, A)]
      
      val newEstimate = current.estimate + stepSize.value*( reward.value + discountRate.value * maxNextValue( current ) - current.estimate )
      
      current.copy( estimate = newEstimate )
   }
   
   def next( current: State ): Action[State] =  {

      val random = Random.nextDouble()    
      if ( random < epsilon.value  ) { // Epsilon
         
         val filtered = filterFromCurrent( internalState, current)
         val res = getRandom( filtered ).action
         
         logger.info( s"Epsilon from $current -> $res" )
         metric.metricNext( metric.Epsilon, current, res )
         
         res
         
      } else  {
         
         val res = getGreedy( internalState, current ).action 
         
         logger.info( s"Greedy from $current -> $res" )
         metric.metricNext( metric.Greedy, current, res )
         
         res
         
      } 
      
   }
   
   def reward( state: State, action: Action[State], reward: Reward ): Try[Unit] = { 
      val stateFiltered = internalState filter{ 
         s =>  s.action == action && s.state == state
      }
      
      stateFiltered match {
         
         case currentQ :: Nil => {
            
            val newEstimate = calculate( currentQ, reward )
            
            logger.info( s"newEstimate:  ( ${currentQ}, $reward) ->  ${newEstimate.estimate} "  )
            
            val filtered = internalState filterNot {  _ == currentQ } 
                            
            internalState = filtered :+ newEstimate
            
            Success( () )
         }
         
         case other => Failure( new IllegalArgumentException( "Pair state-action is not an internal State" ) )
            
      }
   }
}
