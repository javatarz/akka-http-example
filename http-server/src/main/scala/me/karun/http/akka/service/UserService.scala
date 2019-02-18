package me.karun.http.akka.service

import me.karun.http.akka.UserRegistryActor.ActionPerformed
import me.karun.http.akka.models.{User, Users}

import scala.concurrent.Future

trait UserService {
  def fetchAll(): Future[Users]
  def create(user: User): Future[ActionPerformed]
  def find(name: String): Future[Option[User]]
  def delete(name: String): Future[ActionPerformed]
}
