name := """circe-extensions"""

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.8"

// Change this to another test framework if you prefer
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-generic-extras",
  "io.circe" %% "circe-java8",
  "io.circe" %% "circe-parser"
).map(_ % "0.6.0")
