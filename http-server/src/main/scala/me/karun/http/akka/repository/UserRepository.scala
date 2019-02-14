package me.karun.http.akka.repository

import me.karun.http.akka.db.InMemoryNeo4j._
import me.karun.http.akka.models.UserExtension.RecordExtension
import me.karun.http.akka.models.{User, Users}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class UserRepository(implicit executionContext: ExecutionContext) {

  def fetchAll(): Future[Users] = {
    val query = "MATCH (u:User) RETURN u"
    readAllAysnc(query, Map())
      .map(records => Users(records.map(_.toUser("u")).toSet))
  }

  def create(user: User): Try[User] = {
    val query =
      "CREATE (u:User {name:$name, age:$age, countryOfResidence:$countryOfResidence}) RETURN u"
    write(query, user.toMap).map(_.head.toUser("u"))
  }

  def delete(userIdentifier: String): Try[Unit] = {
    val query = "MATCH (u: User {name:$name}) DETACH DELETE u"
    write(query, Map("name" -> userIdentifier)).map(_ => {})
  }
}
