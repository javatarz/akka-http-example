package me.karun.http.akka

import akka.actor.{Actor, ActorLogging, Props}
import com.typesafe.scalalogging.LazyLogging
import me.karun.http.akka.models.User
import me.karun.http.akka.repository.UserRepository

import scala.concurrent.ExecutionContext
import scala.util.Try

object UserRegistryActor {

  def props: Props = Props[UserRegistryActor]

  final case class ActionPerformed(description: String)

  final case class CreateUser(user: User)

  final case class GetUser(name: String)

  final case class DeleteUser(name: String)

  final case object GetUsers

  implicit class ActionPerformedExtensions(user: Try[Any]) extends LazyLogging {
    def toCreateActionPerformed(name: String): ActionPerformed =
      ActionPerformed(s"$suffix creating User $name.")

    def toDeleteActionPerformed(name: String): ActionPerformed =
      ActionPerformed(s"$suffix deleting User named $name.")

    def suffix: String = {
      if (user.isSuccess)
        "Successful in"
      else {
        logger.error("Failed while performing write action to DB.",
                     user.failed.get)
        "Failed while"
      }
    }
  }

}

class UserRegistryActor() extends Actor with ActorLogging {

  import UserRegistryActor._

  private implicit val executionContext: ExecutionContext =
    this.context.dispatcher

  private val userRepository = new UserRepository()

  def receive: Receive = onMessage()

  private def onMessage(): Receive = {
    case GetUsers =>
      sender() ! userRepository.fetchAll()
    case CreateUser(user) =>
      sender() ! userRepository.create(user).toCreateActionPerformed(user.name)
    case GetUser(name) =>
      sender() ! userRepository.fetchAll().map(_.users.find(_.name == name))
    case DeleteUser(name) =>
      sender() ! userRepository.delete(name).toDeleteActionPerformed(name)
  }

}
