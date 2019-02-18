package me.karun.http.akka

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import me.karun.http.akka.db.InMemoryNeo4j
import me.karun.http.akka.repository.UserRepository

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.Source
import scala.util.{Failure, Success}

object QuickstartServer extends App with UserRoutes {

  implicit val system: ActorSystem = ActorSystem("http")
  private implicit val materializer: ActorMaterializer = ActorMaterializer()
  private implicit val executionContext: ExecutionContext = system.dispatcher

  override def userRepository: UserRepository = new UserRepository()

  val userRegistryActor: ActorRef = {
    preloadCypher()
    system.actorOf(UserRegistryActor.props, "userRegistryActor")
  }

  val serverBinding: Future[Http.ServerBinding] =
    Http().bindAndHandle(userRoutes, "localhost", 8080)

  private def preloadCypher(): Unit = {
    val cypher = Source
      .fromURL(getClass.getResource("/preload-graph.cypher"))
      .mkString

    InMemoryNeo4j.run(cypher, Map())

    logger.info("Successfully loaded data to database")
  }

  serverBinding.onComplete {
    case Success(bound) =>
      println(
        s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
    case Failure(e) =>
      Console.err.println(s"Server could not start!")
      e.printStackTrace()
      system.terminate()
  }

  Await.result(system.whenTerminated, Duration.Inf)
}
