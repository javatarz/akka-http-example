import Libs._
import sbt.librarymanagement.ModuleID

object Dependencies {
  private val serverCompile = Seq(Akka.http,
    Akka.stream,
    Akka.`spray-json`,
    Common.`scala-logging`,
    Common.`logger-api`,
    Common.logger,
    Common.`scala-java8-compat`)

  private val serverTest = Seq(Akka.TestOnly.scalatest,
    Akka.TestOnly.`http-test-kit`,
    Akka.TestOnly.`test-kit`)

  private val neo4jCompile = Seq(
    Neo4j.neo4j,
    Neo4j.`neo4j-kernel`,
    Neo4j.`neo4j-java-driver`,
    Neo4j.`neo4j-harness`,
    Neo4j.`neo4j-io`
  )

  val server: Seq[ModuleID] = serverCompile ++ serverTest
  val neo4j: Seq[ModuleID] = neo4jCompile
}
