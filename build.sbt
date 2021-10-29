name := "circuitbreaker-agent"

version := "1.0"

scalaVersion := "2.13.4"

cancelable in Global := true
// fork in run := true

fork := true

scalacOptions += "-deprecation"

libraryDependencies ++= Seq(
  Dependencies.scalatest % "test",
  Dependencies.scalatestFunsuite % "test"
)



libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"




