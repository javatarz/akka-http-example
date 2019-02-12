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

  object Neo4j {

    val neo4j = "org.neo4j" % "neo4j" % "3.3.2"
    val `neo4j-java-driver` = "org.neo4j.driver" % "neo4j-java-driver" % "1.5.1"

    val `neo4j-kernel` = "org.neo4j" % "neo4j-kernel" % "3.3.2"
    val `neo4j-harness` = "org.neo4j.test" % "neo4j-harness" % "3.3.2"
    val `neo4j-io` = "org.neo4j" % "neo4j-io" % "3.3.2"
  }

  object Common {
    private val `logging-version` = "1.8.0-beta2"

    val `scala-logging` = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"
    val `logger-api` = "org.slf4j" % "slf4j-api" % `logging-version`
    val logger = "org.slf4j" % "slf4j-simple" % `logging-version`
    val `scala-java8-compat` = "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0"
  }

}
