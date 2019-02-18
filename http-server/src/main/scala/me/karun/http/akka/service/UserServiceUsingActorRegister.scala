package me.karun.http.akka.service
import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import me.karun.http.akka.UserRegistryActor._
import me.karun.http.akka.models.{User, Users}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class UserServiceUsingActorRegister(implicit val system: ActorSystem,
                                    userRegistryActor: ActorRef,
                                    executionContext: ExecutionContext)
    extends UserService
    with LazyLogging {
  override def fetchAll(): Future[Users] =
    (userRegistryActor ? GetUsers).mapTo[Future[Users]].flatMap(a => a)

  override def create(user: User): Future[ActionPerformed] =
    (userRegistryActor ? CreateUser(user)).mapTo[ActionPerformed]

  override def find(name: String): Future[Option[User]] =
    (userRegistryActor ? GetUser(name))
      .mapTo[Future[Option[User]]]
      .flatMap(a => a)

  override def delete(name: String): Future[ActionPerformed] =
    (userRegistryActor ? DeleteUser(name)).mapTo[ActionPerformed]

  implicit lazy val timeout: Timeout = Timeout(5.seconds)
}
