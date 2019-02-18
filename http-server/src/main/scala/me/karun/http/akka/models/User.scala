package me.karun.http.akka.models

import org.neo4j.driver.v1.Record

final case class User(name: String, age: Int, countryOfResidence: String) {
  def toMap: Map[String, Any] =
    Map("name" -> name,
      "age" -> age,
      "countryOfResidence" -> countryOfResidence)
}

object UserExtension {

  implicit class RecordExtension(record: Record) {
    def toUser(returnReference: String): User = {
      val node = record.get(returnReference)
      User(name = node.get("name").asString(),
        age = node.get("age").asInt(),
        countryOfResidence = node.get("countryOfResidence").asString())
    }
  }

}
