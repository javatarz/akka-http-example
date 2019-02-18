package me.karun.http.akka

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import me.karun.http.akka.db.InMemoryNeo4j
import me.karun.http.akka.repository.UserRepository
import me.karun.http.akka.service.{
  UserServiceUsingActorRegister,
  UserServiceUsingDB
}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.Source
import scala.util.{Failure, Success}

object QuickstartServer extends App with LazyLogging {

  def start(): Unit = {
    implicit val system: ActorSystem = ActorSystem("http")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContext = system.dispatcher

    preloadCypher()

    implicit val userRegistryActor: ActorRef =
      system.actorOf(UserRegistryActor.props, "userRegistryActor")

    val userRoutes = new UserRoutes {
      override def userRoutesUsingDB: AbstractUserRoutes =
        new AbstractUserRoutes(new UserServiceUsingDB(new UserRepository())) {}

      override def userRoutesUsingActors: AbstractUserRoutes =
        new AbstractUserRoutes(new UserServiceUsingActorRegister()) {}
    }.userRoutes

    val serverBinding: Future[Http.ServerBinding] =
      Http().bindAndHandle(userRoutes, "localhost", 8080)

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

  private def preloadCypher(): Unit = {
    val cypher = Source
      .fromURL(getClass.getResource("/preload-graph.cypher"))
      .mkString

    InMemoryNeo4j.run(cypher, Map())

    logger.info("Successfully loaded data to database")
  }

  start()
}
