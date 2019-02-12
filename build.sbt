organization := "me.karun"
name := "akka-http-example"
version := "0.1"
val `scala-version` = "2.11.8"
scalaVersion := `scala-version`

lazy val root = (project in file("."))
  .aggregate(`http-server`)

import Dependencies._

lazy val `http-server` = project
    .settings(scalaVersion.:=(`scala-version`))
  .settings(libraryDependencies ++= server)
  .settings(libraryDependencies ++= neo4j)