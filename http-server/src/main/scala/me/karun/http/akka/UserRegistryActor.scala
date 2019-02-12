package me.karun.http.akka

import akka.actor.{Actor, ActorLogging, Props}
import me.karun.http.akka.models.{User, Users}


object UserRegistryActor {

  def props: Props = Props[UserRegistryActor]

  final case class ActionPerformed(description: String)

  final case class CreateUser(user: User)

  final case class GetUser(name: String)

  final case class DeleteUser(name: String)

  final case object GetUsers

}

class UserRegistryActor extends Actor with ActorLogging {

  import UserRegistryActor._

  private val users = Set.empty[User]

  def receive: Receive = onMessage(users)

  private def onMessage(users: Set[User]): Receive = {
    case GetUsers =>
      sender() ! Users(users.toSeq)
    case CreateUser(user) =>
      context.become(onMessage(users + user))
      sender() ! ActionPerformed(s"User ${user.name} created.")
    case GetUser(name) =>
      sender() ! users.find(_.name == name)
    case DeleteUser(name) =>
      val deletedUserCount = users.find(_.name == name)
        .map(user => {
          context.become(onMessage(users - user))
          1
        })
        .sum

      val suffix = if (deletedUserCount == 1) "" else "s"
      sender() ! ActionPerformed(s"$deletedUserCount user$suffix named $name were deleted.")
  }
}