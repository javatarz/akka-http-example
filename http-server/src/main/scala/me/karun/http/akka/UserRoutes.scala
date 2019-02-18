package me.karun.http.akka

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import me.karun.http.akka.UserRegistryActor._
import me.karun.http.akka.models.{JsonSupport, User}
import me.karun.http.akka.repository.UserRepository

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Try

trait UserRoutes extends JsonSupport with LazyLogging {

  implicit def system: ActorSystem

  def userRepository: UserRepository

  private implicit val executionContext: ExecutionContext = system.dispatcher

  lazy val userRoutes: Route =
    pathPrefix("users") {
      concat(
        pathEnd {
          concat(
            get {
              logger.info("Get all users api was called.")
              complete(userRepository.fetchAll())
            },
            post {
              entity(as[User]) { user =>

                val suffix = resolveTry(userRepository.create(user))
                val performed = ActionPerformed(s"$suffix creating User ${user.name}")

                logger.info(s"Created user [${user.name}]: ${performed.description}")
                complete((StatusCodes.Created, performed))
              }
            }
          )
        },
        path(Segment) { name =>
          concat(
            get {
              rejectEmptyResponse {
                complete(userRepository.fetchAll().map(_.users.find(_.name == name)))
              }
            },
            delete {

              val suffix = resolveTry(userRepository.delete(name))
              val performed = ActionPerformed(s"$suffix deleting User named $name.")

              logger.info(s"Deleted user [$name]: ${performed.description}")
              complete((StatusCodes.OK, performed))
            }
          )
        }
      )
    }

  implicit lazy val timeout: Timeout = Timeout(5.seconds)

  private def resolveTry(result: Try[Any]): String = {
    val suffix =
      if (result.isSuccess)
        "Successful in"
      else {
        logger.error("Failed while performing write action to DB.", result.failed.get)
        "Failed while"
      }
    suffix
  }
}
