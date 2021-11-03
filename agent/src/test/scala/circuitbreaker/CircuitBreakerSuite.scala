package com.logicaalternativa.circuitbreakeragent
package circuitbreaker

import org.scalatest.funsuite.AnyFunSuite

import mother.data._
import mother.clock._
import scala.util._
import scala.concurrent.duration._
import scala.concurrent._
    
  

object CircuitBreakerSuite {
  
  case class ExceptionDtoIn( dto: DtoIn ) extends Throwable
  
  implicit val ec: ExecutionContext = ExecutionContext.global
  
  def connectorFactory( expectedResult: Try[DtoOut], delay: Duration = Duration.Zero ) = new Connector[DtoIn, Try, DtoOut] {
    
    override def apply( dtoIn: DtoIn ): Try[DtoOut] = {
      
      val after = System.currentTimeMillis
      
      Thread.sleep( delay.toMillis )
      
      expectedResult
      
    }
    
  }
  
  val durationHalfOpen = 500.millis
   
  val learning : Learning = StandardLearning( durationHalfOpen )
  
  val dtoIn = DtoIn( Timestamp( 1 ) )
    
  val expectedResultOk = Success( DtoOut( dtoIn ) )
  
  val errorOpenState: DtoIn => Throwable = v1 => ExceptionDtoIn( v1 )
    
  val expectedResult = Failure( new RuntimeException( "Error connection" ) )
    
  val times = Random.nextInt( 10 ) + 1  
  
  val expectedErrorOpenState = Failure( ExceptionDtoIn( dtoIn ) )
        
    
}


class CircuitBreakerSuite extends AnyFunSuite {
  
  import CircuitBreakerSuite._
  import scala.language.postfixOps
  
  
  test("Validate correct value, Happy Path") {
    
    val config = ConfigCircuitBreaker( 1, 500 millis )
    
    val circuitBreaker = CircuitBreakerSync( connectorFactory( expectedResultOk ), config, errorOpenState, learning )
    
    assert( circuitBreaker( dtoIn ) == expectedResultOk )
      
  }
  
  test("Validate several error") {
    
    def validateCicle( circuitBreaker: Connector[DtoIn,Try,DtoOut]) : Unit = {
      
      for ( reg <- 0 until times  ) { 
        assert( circuitBreaker( dtoIn ) == expectedResult, s"The first errors should be the expected result: ${reg + 1} from ${times}" )
      }
    
      assert( circuitBreaker( dtoIn ) == expectedErrorOpenState, "The second error should be the OpenState" )
      
    }
    
    val config = ConfigCircuitBreaker( times, 500 millis )
    
    val circuitBreaker = CircuitBreakerSync( connectorFactory( expectedResult ), config, errorOpenState, learning )
    
    validateCicle( circuitBreaker )
    
    Thread.sleep( durationHalfOpen.toMillis + 500 )    
    
    
    assert( circuitBreaker( dtoIn ) == expectedErrorOpenState, "The second error should be the OpenState (Internal state should be Half Open State)" )
    
    assert( circuitBreaker( dtoIn ) == expectedResult, "The first errors should be the expected result" )
      
  }
  
  
  
  test("Validate several by Timeout") {
    
    val timeout = 100.millis
    
    val config = ConfigCircuitBreaker( times, timeout )
    
    val circuitBreaker = CircuitBreakerSync( connectorFactory( expectedResultOk, timeout + 100.millis ), config, errorOpenState, learning )
    
    for ( _ <- 0 until times ) {
    
      val firstCall = circuitBreaker( dtoIn )
      assert( 
        firstCall match {
           case Failure( e: TimeoutException )  => true
           case other => false
         }, 
        s"The first error should be the a timeout exception, result: ${firstCall}"  
      )
    
    }
    
    assert( circuitBreaker( dtoIn ) == expectedErrorOpenState, "The second error should be the OpenState" )
      
  }
  
} 
