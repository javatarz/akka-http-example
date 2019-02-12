import sbt.stringToOrganization

object Libs {

  object Akka {
    private val akkaHttpVersion = "10.1.7"
    private val akkaVersion = "2.5.20"

    val http = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
    val stream = "com.typesafe.akka" %% "akka-stream" % akkaVersion
    val `spray-json` = "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion

    object TestOnly {
      val `http-test-kit` = "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % sbt.Test
      val `test-kit` = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % sbt.Test
      val `stream-test-kit` = "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % sbt.Test
      val scalatest = "org.scalatest" %% "scalatest" % "3.0.5" % sbt.Test
    }
  }

  object Common {
    private val `logging-version` = "1.8.0-beta2"

    val `scala-logging` = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
    val `logger-api` = "org.slf4j" % "slf4j-api" % `logging-version`
    val logger = "org.slf4j" % "slf4j-simple" % `logging-version`
  }

}
