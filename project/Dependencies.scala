import sbt._

object Dependencies {
 object versions {
    val scalatest = "3.2.2"

  }
 val scalatest =  "org.scalatest" %% "scalatest" % versions.scalatest
 val scalatestFunsuite = "org.scalatest" %% "scalatest-funsuite" % versions.scalatest
}
