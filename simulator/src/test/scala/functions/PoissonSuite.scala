package com.logicaalternativa.circuitbreakeragent
package simulator

import org.scalatest.funsuite.AnyFunSuite

import mother.NumberCalls
import clock._
import functions._
import circuitbreaker._
import report._
import scala.util._
import scala.concurrent._

import scala.concurrent.duration._
import scala.language.postfixOps

import scala.annotation.tailrec  


class PoissonSuite extends AnyFunSuite {
  
  import LoadSimulatorSuite._
  import clock._  
  
  import scala.concurrent._
    
  
  test("Testing mean Poisson") {
    
    val expectedMean = 5
    
    val posison = Poisson( expectedMean )
    
    @tailrec
    def _iterative( cont: Double, sum: Int, max : Int, min: Int ) : (Int, Int, Int) = {
        
      if ( cont == 0 ) {
        
        ( sum, max, min )
        
      } else {
      
        val res = posison(1)
        
        _iterative( cont -1, sum + res, if( res > max ) res else max, if( res < min ) res else min )
        
      }
      
    } 
    
    val number = Random.nextInt( 2000 ) + 500
    
    val ( sum, max, min ) = _iterative( number, 0, 0, 10000 )
    
    val mean  = sum / number.toDouble
    
    // Traza
    //~ println( s"hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh mean: ${mean}, max ${max}, min ${min}" )
    // Fin de traza
    
    assert( Math.round( mean ) == expectedMean, "Mean shoud be the same "  )
    
    
  }
  
} 
