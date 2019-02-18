package me.karun.http.akka.db

import org.neo4j.driver.v1._

import scala.collection.JavaConverters._
import scala.compat.java8.FutureConverters.CompletionStageOps
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object DBExtensions {
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
