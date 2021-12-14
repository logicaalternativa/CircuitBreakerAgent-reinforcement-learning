package com.logicaalternativa.circuitbreakeragent
package circuitbreaker

import scala.util._
import scala.concurrent.ExecutionContext
import scala.concurrent._
import scala.concurrent.duration._

import org.slf4j._
import java.util.concurrent.atomic.{AtomicReference, AtomicInteger}

import CircuitBreakerState._
import CircuitBreakerActions._

  
trait Connector[A, P[_], B]  extends (A => P[B])

object CircuitBreakerSync {
  
  import agent._
  
  val logger = LoggerFactory.getLogger("com.logicaalternativa.circuitbreakeragent.circuitbreaker.CircuitBreakerSync");
  
  case class DurationHalfOpen( value: Duration ) extends AnyVal

  case class InfoLearning(
    fromDuration: DurationHalfOpen,
    fromAction: Action[Duration]
  )

  case class State( 
                    state : CircuitBreakerState =  Closed, 
                    durationHalfOpen: Duration,
                    infoLearning: InfoLearning
                  )
  
  val stateMachineClosed = StateMachine( Closed )
  val stateMachineOpen = StateMachine( Open )
  val stateMachineHalfOpen = StateMachine( HalfOpen )
  
  def apply[A,B]( connector: Connector[A,Try,B], config: ConfigCircuitBreaker, errorOpenState: A => Throwable, learning: Learning)( using ec: ExecutionContext ) = new CircuitBreakerSync[A,B]( connector, config, errorOpenState, learning )
  
  protected def calculateCallsError( isSuccesfull: Boolean, callsWithError: Int ) = 
  
    if ( isSuccesfull ) 
       0
    else 
      callsWithError + 1
      
  
  def getActionClosed(numberCallsWithError: Int,  config: ConfigCircuitBreaker ) : CircuitBreakerActions = {
    if( numberCallsWithError >= config.numberOfErrors ) {
      Ko
    } else {
      Ok 
    }
  }
  
  def getActionHalfClosed(isSuccesfull: Boolean ) : CircuitBreakerActions = {
    
    logger.info(s"getActionHalfClosed isSuccesfull? ${isSuccesfull}" )
    
    if( ! isSuccesfull ) {
      Ko
    } else {
      Ok 
    }
  }
  
  protected def getActionOpen( timeLastExecution: Long, durationHalfOpen: Duration ): CircuitBreakerActions = {
    
    logger.info(s"getActionOpen condition: ${System.currentTimeMillis - timeLastExecution > durationHalfOpen.toMillis} :: ${durationHalfOpen}" )
    
    if ( System.currentTimeMillis - timeLastExecution < durationHalfOpen.toMillis ) {
      FailFast
    } else {
      AttemptReset
    }
    
  }
  
  object Episode {
    
    def apply() = new Episode {
    
      private var requests = 0D
      private var succesfulls = 0D
      
      def totalPlusOne = synchronized { 
        requests = requests + 1D 
        logger.info( s"add new request: ${requests}" )
      }
      def successPlusOne = synchronized { 
        succesfulls = succesfulls + 1D
        logger.info( s"add request ok: ${succesfulls}" ) 
      }
      
      def ratio: Double = succesfulls / requests
      
      def init: Unit = synchronized {
        requests = 0D
        succesfulls = 0D 
      } 
    }
  }
  
  sealed trait Episode {
    def totalPlusOne: Unit
    def successPlusOne: Unit
    def ratio: Double
    def init: Unit
    
  }  
  
}

class CircuitBreakerSync[A, B]( connector: Connector[A,Try,B], config: ConfigCircuitBreaker, errorOpenState: A => Throwable, learning: Learning)( using ec: ExecutionContext )  extends Connector[A,Try,B] {
  
  import CircuitBreakerSync._
  
  val state : AtomicReference[CircuitBreakerState] =  new AtomicReference( Closed )
  
  val episode = Episode()
  
  var timeLastExecution: Long = 0  
  
  val callsWithError = new AtomicInteger( 0 ) 
  
  var (durationHalfOpen, infoLearning ) =  {
    
    val fromDuration = learning.initHalfOpenDuration   
    
    val action = learning.nextHalfOpenDuration( fromDuration )
    logger.info( s"[initState] first state ${fromDuration}, action: ${action}" )
    
    val durationHalfOpen = action( fromDuration )
    
    val infoLearning = InfoLearning( fromAction = action, fromDuration = DurationHalfOpen( fromDuration ) ) 
    (DurationHalfOpen( durationHalfOpen ), infoLearning )
    
  }
  
  
  
  private def optimisticLockUpdate( before: CircuitBreakerState, now: CircuitBreakerState ): Boolean = {
    
    if ( before == now ) {
      logger.info( s" No update the state are the same ${before} -> ${now}" )
      true
      
    } else {
   
      val ts = java.util.UUID.randomUUID.toString
      logger.info( s" [${ts}] Triying change the state ${before} -> ${now}" )
      val res = state.compareAndSet( before, now ) 
      
      if ( res ) {
         logger.info( s"[${ts}] [+] Update the state ${before} -> ${now}: ${System.currentTimeMillis - timeLastExecution}" )
      } else {
        logger.warn( s" [${ts}] [-] It is not posible save the state ${before} -> ${now}" )
      }
      
      res
    }
  }
  
  private def executeConnector( v1: A ): Try[B] = {
    
    val resF = Future{ connector( v1 ).get }
    
    val res = Try{ Await.result( resF, config.timeout ) }
    
    timeLastExecution = System.currentTimeMillis
    
    if (res.isSuccess ) {
      
      callsWithError.set( 0 )
      
      episode.successPlusOne

      
    } else {
      
      callsWithError.getAndIncrement()
      
    }
    
    res
     
  }
  
  private def executeClosed( v1: A ): Try[B] = {
    
    val res = executeConnector( v1 )
    
    val action = getActionClosed( callsWithError.get, config )
    
    stateMachineClosed( action ) match {
      case Success( value ) => optimisticLockUpdate( Closed, value ) 
      case Failure( error ) => logger.error( s"There is an error to get the next state, current state: Closed, action: ${action}" )      
    }
    
    res    
  }
  
  private def executeOpen( v1: A ) : Try[B] = {
    
    val cbAction = getActionOpen( timeLastExecution, durationHalfOpen.value )
    
    stateMachineOpen( cbAction ) match {
      case Success( value ) => optimisticLockUpdate( Open, value ) 
      case Failure( error ) => logger.error( s"There is an error getting the next state, current state: s{currentState.state}, action: ${cbAction}" ) 
    }
        
    Failure( errorOpenState( v1 ) )
  }
  
  private def executeHalfOpen(  v1: A ): Try[B] = {
    
    val res = executeConnector( v1 )
    
    val cbAction = getActionHalfClosed( res.isSuccess )
    
    stateMachineHalfOpen( cbAction ) match {
      case Success( Open ) => logger.info( s"stateMachineHalfOpen -> Open" )
                               
                                if ( optimisticLockUpdate( HalfOpen, Open ) ) {
                                  
                                  updateValueLearning( false )
                                  
                                }
                                
      case Success( Closed ) => logger.info( s"stateMachineHalfOpen -> Closed" )
                               
                                if ( optimisticLockUpdate( HalfOpen , Closed ) ) {
                                  
                                  updateValueLearning( true )
                                  
                                }
      case Success( HalfOpen ) => logger.error( s"From HalfOpen to HalfOpen is not allowed" ) 
      
      case Failure( error ) => logger.error( s"There is an error getting the next state, current state: s{currentState.state}, action: ${cbAction}" ) 
    }
    
    res
  }
  
  private def updateValueLearning( isSuccesfull: Boolean ): Unit = synchronized {
    
    val ratio = episode.ratio
    
    val result = learning.publish( infoLearning.fromDuration.value, infoLearning.fromAction, ratio, isSuccesfull )    

    logger.info(s"[updateValueLearning] ratio: ${ratio}, isSuccesfull? ${episode.ratio}, result publish: ${result}")
    
    if ( isSuccesfull ) { episode.init }    
    
    infoLearning = InfoLearning(
      fromDuration = durationHalfOpen, 
      fromAction = learning.nextHalfOpenDuration( durationHalfOpen.value  )
    )
    
    durationHalfOpen = DurationHalfOpen( infoLearning.fromAction( durationHalfOpen.value  ) )
    
    logger.info(s"[updateValueLearning] newDurationHalfOpen ${durationHalfOpen} from ${infoLearning.fromDuration} execution ${infoLearning.fromAction}")     

  }
  
  override def apply(v1: A): Try[B] = {
    
    episode.totalPlusOne;
    
    state.get match {
        case Open     => executeOpen( v1 )
        case Closed   => executeClosed( v1 )
        case HalfOpen => executeHalfOpen( v1 )
    }
    
  }

}

