package me.karun.http.akka.db

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import org.neo4j.driver.v1._
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.kernel.configuration.BoltConnector
import org.neo4j.test.TestGraphDatabaseFactory

import scala.collection.JavaConverters._
import scala.compat.java8.FutureConverters.CompletionStageOps
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object InMemoryNeo4j extends LazyLogging {

  private val startPortNumber = 10000
  private val endPortNumber = 65535
  private val port = startPortNumber + (new scala.util.Random)
    .nextInt((endPortNumber - startPortNumber) + 1)

  private val dbStorePath =
    new File(System.getProperty("java.io.tmpdir"),
             java.util.UUID.randomUUID().toString + ".db")
  private val connector = new BoltConnector()

  private val hostNamePort = s"localhost:$port"
  private val database: GraphDatabaseService = new TestGraphDatabaseFactory()
    .newImpermanentDatabaseBuilder(dbStorePath)
    .setConfig(connector.`type`, "BOLT")
    .setConfig(connector.enabled, "true")
    .setConfig(connector.listen_address, hostNamePort)
    .setConfig(connector.encryption_level,
               connector.encryption_level.getDefaultValue)
    .newGraphDatabase()

  private val driver: Driver =
    GraphDatabase.driver(s"bolt://$hostNamePort")

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

  implicit class SessionExtensions(session: Session) {

    def readAllAsync(query: String, params: Map[String, AnyRef])(
        pf: StatementResultCursor => Future[java.util.List[Record]])(
        implicit executionContext: ExecutionContext): Future[Seq[Record]] = {
      val result = session
        .runAsync(query, params.asJava)
        .toScala
        .flatMap(pf)
        .map(_.asScala.toSeq)
      session.closeAsync()
      result
    }

    def write(query: String, params: Map[String, Any]): Try[Seq[Record]] = {
      session.writeTransaction(new TransactionWork[Try[Seq[Record]]] {
        override def execute(tx: Transaction): Try[Seq[Record]] = {
          Try {
            tx.run(query,
                   params.map(t => (t._1, t._2.asInstanceOf[AnyRef])).asJava)
              .asScala
              .toSeq
          }.map(records => {
            tx.success()
            session.close()
            records
          })
        }
      })
    }
  }
}
