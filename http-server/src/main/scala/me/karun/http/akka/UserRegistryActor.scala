package me.karun.http.akka

import akka.actor.{Actor, ActorLogging, Props}
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
      val suffix = resolveTry(userRepository.create(user))
      sender() ! ActionPerformed(s"$suffix creating User ${user.name} ")
    case GetUser(name) =>
      sender() ! userRepository.fetchAll().map(_.users.find(_.name == name))
    case DeleteUser(name) =>
      val suffix = resolveTry(userRepository.delete(name))
      sender() ! ActionPerformed(s"$suffix deleting User named $name.")
  }

  private def resolveTry(result: Try[Any]): String = {
    val suffix =
      if (result.isSuccess)
        "Successful in"
      else {
        log.error(result.failed.get, "Failed while performing write action to DB.")
        "Failed while"
      }
    suffix
  }
}
