organization := "me.karun"
name := "akka-http-example"
version := "0.1"
scalaVersion := "2.12.8"

lazy val root = (project in file("."))
  .aggregate(`http-server`)

import Dependencies._

lazy val `http-server` = project
  .settings(libraryDependencies ++= server)