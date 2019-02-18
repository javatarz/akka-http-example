package me.karun.http.akka

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import com.typesafe.scalalogging.LazyLogging
import me.karun.http.akka.models.{JsonSupport, User}
import me.karun.http.akka.service.UserService

import scala.concurrent.ExecutionContext

abstract class AbstractUserRoutes(service: UserService)(
    implicit val system: ActorSystem,
    executionContext: ExecutionContext)
    extends JsonSupport
    with LazyLogging {

  lazy val userRoutes: Route =
    pathPrefix("users") {
      concat(
        pathEnd {
          concat(
            get {
              logger.info("Get all users api was called.")
              complete(service.fetchAll())
            },
            post {
              entity(as[User]) { user =>
                onSuccess(service.create(user)) { performed =>
                  logger.info(
                    s"Created user [${user.name}]: ${performed.description}")
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
                complete(service.fetchAll().map(_.users.find(_.name == name)))
              }
            },
            delete {
              onSuccess(service.delete(name)) { performed =>
                logger.info(s"Deleted user [$name]: ${performed.description}")
                complete((StatusCodes.OK, performed))
              }
            }
          )
        }
      )
    }

}
