package me.karun.http.akka.service
import me.karun.http.akka.UserRegistryActor._
import me.karun.http.akka.models.{User, Users}
import me.karun.http.akka.repository.UserRepository

import scala.concurrent.{ExecutionContext, Future}

class UserServiceUsingDB(repository: UserRepository)(
    implicit executionContext: ExecutionContext)
    extends UserService {
  override def fetchAll(): Future[Users] = repository.fetchAll()

  override def create(user: User): Future[ActionPerformed] =
    Future.successful(
      repository.create(user).toCreateActionPerformed(user.name))

  override def find(name: String): Future[Option[User]] =
    repository.fetchAll().map(_.users.find(_.name == name))

  override def delete(name: String): Future[ActionPerformed] =
    Future.successful(repository.delete(name).toDeleteActionPerformed(name))
}
