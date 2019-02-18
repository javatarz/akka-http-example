package me.karun.http.akka

import akka.http.scaladsl.server.Directives.{concat, pathPrefix}
import akka.http.scaladsl.server.Route
import me.karun.http.akka.models.JsonSupport

trait UserRoutes extends JsonSupport {
  def userRoutesUsingDB: AbstractUserRoutes
  def userRoutesUsingActors: AbstractUserRoutes

  val userRoutes: Route = concat(
    userRoutesUsingDB.userRoutes,
    pathPrefix("v1") {
      userRoutesUsingActors.userRoutes
    }
  )
}
