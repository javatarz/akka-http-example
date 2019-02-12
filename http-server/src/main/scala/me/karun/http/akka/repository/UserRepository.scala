package me.karun.http.akka.repository

import me.karun.http.akka.db.InMemoryNeo4j
import me.karun.http.akka.models.User

class UserRepository() {

  def fetchAllUsers(): Set[User] = {
    val query = "MATCH (u:User) RETURN u"
    InMemoryNeo4j
      .run(query, Map())
      .map(record => {
        val node = record.get("u")
        User(name = node.get("name").asString(),
             age = node.get("age").asInt(),
             countryOfResidence = node.get("countryOfResidence").asString())
      })
      .toSet
  }
}
