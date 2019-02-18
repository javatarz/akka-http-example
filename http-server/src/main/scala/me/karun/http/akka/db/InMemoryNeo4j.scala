package me.karun.http.akka.db

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import me.karun.http.akka.db.DBExtensions._
import org.neo4j.driver.v1._
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.kernel.configuration.BoltConnector
import org.neo4j.test.TestGraphDatabaseFactory

import scala.collection.JavaConverters._
import scala.compat.java8.FutureConverters.CompletionStageOps
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object InMemoryNeo4j extends LazyLogging {

  private val hostNamePort = s"localhost:${randomPort()}"

  createInMemoryDB()

  private val driver: Driver = GraphDatabase.driver(s"bolt://$hostNamePort")

  def run(query: String, params: Map[String, AnyRef]): Iterator[Record] = {
    val session = driver.session()
    val result = session.run(query, params.asJava)
    session.close()
    result
  }.asScala

  def readAllAysnc(query: String, params: Map[String, AnyRef])(
      implicit executionContext: ExecutionContext): Future[Seq[Record]] =
    driver.session().readAllAsync(query, params)(_.listAsync().toScala)

  def write(query: String, params: Map[String, Any]): Try[Seq[Record]] =
    driver.session().write(query, params)

  private def randomPort(startPortNumber: Int = 10000,
                         endPortNumber: Int = 65535): Int = {
    startPortNumber + (new scala.util.Random)
      .nextInt((endPortNumber - startPortNumber) + 1)
  }

  private def createInMemoryDB(): GraphDatabaseService = {
    val dbStorePath = new File(System.getProperty("java.io.tmpdir"),
                               java.util.UUID.randomUUID().toString + ".db")
    val connector = new BoltConnector()
    new TestGraphDatabaseFactory()
      .newImpermanentDatabaseBuilder(dbStorePath)
      .setConfig(connector.`type`, "BOLT")
      .setConfig(connector.enabled, "true")
      .setConfig(connector.listen_address, hostNamePort)
      .setConfig(connector.encryption_level,
                 connector.encryption_level.getDefaultValue)
      .newGraphDatabase()
  }
}
