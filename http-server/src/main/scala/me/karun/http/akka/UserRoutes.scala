package me.karun.http.akka

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import me.karun.http.akka.UserRegistryActor._
import me.karun.http.akka.models.{JsonSupport, User, Users}

import scala.concurrent.duration._

trait UserRoutes extends JsonSupport with LazyLogging {

  implicit def system: ActorSystem
  lazy val userRoutes: Route =
    pathPrefix("users") {
      concat(
        pathEnd {
          concat(
            get {
              complete((userRegistryActor ? GetUsers).mapTo[Users])
            },
            post {
              entity(as[User]) { user =>
                onSuccess((userRegistryActor ? CreateUser(user)).mapTo[ActionPerformed]) { performed =>
                  logger.info(s"Created user [${user.name}]: ${performed.description}")
                  complete((StatusCodes.Created, performed))
                }
              }
            }
          )
        },
        path(Segment) { name =>
          concat(
            get {
              rejectEmptyResponse {
                complete((userRegistryActor ? GetUser(name)).mapTo[Option[User]])
              }
            },
            delete {
              onSuccess((userRegistryActor ? DeleteUser(name)).mapTo[ActionPerformed]) { performed =>
                logger.info(s"Deleted user [$name]: ${performed.description}")
                complete((StatusCodes.OK, performed))
              }
            }
          )
        }
      )
    }

  // Required by the `ask` (?) method below
  implicit lazy val timeout: Timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  def userRegistryActor: ActorRef
}
