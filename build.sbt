
ThisBuild / organization := "com.logicaalternativa"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.0.2"


name := "circuitbreaker-agent"

cancelable in Global := true

fork := true



lazy val global = project.in(file("."))
  .aggregate(
    mother,
    agent,
    simulator
  )
  
  
lazy val mother =  (project in file("mother"))
  .settings(
    name := "mother",
    libraryDependencies ++= commonDependencies
  )

lazy val agent =  (project in file("agent"))
  .settings(
    name := "agent",
    libraryDependencies ++= commonDependencies
    
  ).dependsOn(
    mother % "test->compile"
  )


lazy val simulator =  (project in file("simulator"))
  .settings(
    name := "simulator",
    libraryDependencies ++= commonDependencies
  )
  .dependsOn(
    agent,
    mother
  )

scalacOptions += "-deprecation"



lazy val commonDependencies = Seq (
    "ch.qos.logback" % "logback-classic" % "1.2.5",
    Dependencies.scalatest         % "test",
    Dependencies.scalatestFunsuite % "test"
)






