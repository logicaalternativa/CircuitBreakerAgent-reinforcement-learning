package com.logicaalternativa.circuitbreakeragent
package agent

import org.scalatest.funsuite.AnyFunSuite

import scala.util._
import test._



object AgentSuite {
  
  
}


class AgentSuite extends AnyFunSuite {
  
  import AgentSuite._
  
  test("Testing") {
    
    val change_1 = Action[Int]( _ =>  1)
    val change_2 = Action[Int]( _ =>  2)
    
    val listQ: List[Q[Int]] = Q( 1, change_1, 0 ) ::  
                              Q( 1, change_2, 0 ) :: 
                              Q( 2, change_1, 0 ) :: 
                              Q( 2, change_2, 0 ) :: 
                              Nil
                              
    val agent = QAgent( listQ, StepSize(1).get, DiscountRate(1).get, Epsilon( 0 ).get )
    
    assert( agent.reward( 1, change_1, Reward( 0.90 ) ) == Success( () ) )
    assert( agent.reward( 1, change_2, Reward( 0.45 ) ) == Success( () ) )
    assert( agent.reward( 1, change_2, Reward( 0.45 ) ) == Success( () ) )
    assert( agent.reward( 1, change_2, Reward( 0.45 ) ) == Success( () ) )
    
    assert( agent.next( 1 ) == change_1 ) 
    assert( agent.next( 1 ) == change_1 ) 
    assert( agent.next( 1 ) == change_1 ) 
    assert( agent.next( 1 ) == change_1 ) 
    
    assert( agent.reward( 2, change_1, Reward( 0.90 ) ) == Success( () ) )
    //~ assert( agent.reward( 2, change_1, Reward( 0.45 ) ) == Success( () ) )
    
  }
  
  test("If testing with no epsilon-greedy the state value has to ve the greater estimation") {
    
    val change_1 = Action[Int]( _ =>  1)
    val change_2 = Action[Int]( _ =>  2)
    
    val listQ: List[Q[Int]] = Q( 1, change_1, 0 ) ::  
                              Q( 1, change_2, 0 ) :: 
                              Q( 2, change_1, 0 ) :: 
                              Q( 2, change_2, 0 ) :: 
                              Nil
                              
    val agent = QAgent( listQ, StepSize(1).get, DiscountRate(1).get, Epsilon( 0 ).get )
    
    assert( agent.reward( 1, change_1, Reward( 0.90 ) ) == Success( () ) )
    assert( agent.reward( 2, change_2, Reward( 0.45 ) ) == Success( () ) )
    
    assert( agent.next( 1 ) == change_1 ) 
    assert( agent.next( 2 ) == change_2 )
    
  }
  
  
  test ("error when the pair state-action is not a internal value") {
    
    val change_1 = Action[Int]( _ =>  1)
    val change_2 = Action[Int]( _ =>  2)
    
    val listQ: List[Q[Int]] = Q( 1, change_1, 0 ) ::  
                              Nil
    
    val agent = QAgent( listQ, StepSize(1).get, DiscountRate(1).get, Epsilon( 0 ).get )
    agent.reward( 2, change_1, Reward( 0.90 )  ) match {
      
        case Failure( e ) => assert( e.getMessage == "Pair state-action is not an internal State" )
        case other => fail( s"The method returns other value ${other}" )
    }
    
  }
   
  
    
} 
