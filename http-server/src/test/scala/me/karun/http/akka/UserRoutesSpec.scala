package me.karun.http.akka

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import me.karun.http.akka.db.InMemoryNeo4j
import me.karun.http.akka.models.User
import me.karun.http.akka.repository.UserRepository
import me.karun.http.akka.service.{
  UserServiceUsingActorRegister,
  UserServiceUsingDB
}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.io.Source

class UserRoutesSpec
    extends WordSpec
    with Matchers
    with ScalaFutures
    with ScalatestRouteTest
    with UserRoutes
    with BeforeAndAfterAll {
  "In UserRoutes" when {
    "end-points with in-memory DB is invoked" should {
      "return no users if no present (GET /users)" in {
        // note that there's no need for the host part in the uri:
        val request = HttpRequest(uri = "/users")

        request ~> userRoutes ~> check {
          status should ===(StatusCodes.OK)

          // we expect the response to be json:
          contentType should ===(ContentTypes.`application/json`)

          // and no entries should be in the list:
          entityAs[String] should ===(
            """{"users":[{"age":55,"countryOfResidence":"US","name":"John"},{"age":35,"countryOfResidence":"IN","name":"Rajeev"},{"age":29,"countryOfResidence":"CA","name":"Ryan"}]}""")
        }
      }

      "be able to add users (POST /users)" in {
        val user = User("Kapi", 42, "jp")
        val userEntity = Marshal(user).to[MessageEntity].futureValue // futureValue is from ScalaFutures

        // using the RequestBuilding DSL:
        val request = Post("/users").withEntity(userEntity)

        request ~> userRoutes ~> check {
          status should ===(StatusCodes.Created)

          // we expect the response to be json:
          contentType should ===(ContentTypes.`application/json`)

          // and we know what message we're expecting back:
          entityAs[String] should ===(
            """{"description":"Successful in creating User Kapi."}""")
        }
      }

      "be able to remove users (DELETE /users)" in {
        // user the RequestBuilding DSL provided by ScalatestRouteSpec:
        val request = Delete(uri = "/users/Kapi")

        request ~> userRoutes ~> check {
          status should ===(StatusCodes.OK)

          // we expect the response to be json:
          contentType should ===(ContentTypes.`application/json`)

          // and no entries should be in the list:
          entityAs[String] should ===(
            """{"description":"Successful in deleting User named Kapi."}""")
        }
      }
    }

    "end-points using actor registry is used" should {
      "return no users if no present (GET v1/users)" in {
        // note that there's no need for the host part in the uri:
        val request = HttpRequest(uri = "/v1/users")

        request ~> userRoutes ~> check {
          status should ===(StatusCodes.OK)

          // we expect the response to be json:
          contentType should ===(ContentTypes.`application/json`)

          // and no entries should be in the list:
          entityAs[String] should ===(
            """{"users":[{"age":55,"countryOfResidence":"US","name":"John"},{"age":35,"countryOfResidence":"IN","name":"Rajeev"},{"age":29,"countryOfResidence":"CA","name":"Ryan"}]}""")
        }
      }

      "be able to add users (POST v1/users)" in {
        val user = User("Kapi", 42, "jp")
        val userEntity = Marshal(user).to[MessageEntity].futureValue // futureValue is from ScalaFutures

        // using the RequestBuilding DSL:
        val request = Post("/v1/users").withEntity(userEntity)

        request ~> userRoutes ~> check {
          status should ===(StatusCodes.Created)

          // we expect the response to be json:
          contentType should ===(ContentTypes.`application/json`)

          // and we know what message we're expecting back:
          entityAs[String] should ===(
            """{"description":"Successful in creating User Kapi."}""")
        }
      }

      "be able to remove users (DELETE v1/users)" in {
        // user the RequestBuilding DSL provided by ScalatestRouteSpec:
        val request = Delete(uri = "/v1/users/Kapi")

        request ~> userRoutes ~> check {
          status should ===(StatusCodes.OK)

          // we expect the response to be json:
          contentType should ===(ContentTypes.`application/json`)

          // and no entries should be in the list:
          entityAs[String] should ===(
            """{"description":"Successful in deleting User named Kapi."}""")
        }
      }
    }
  }

  implicit val actorRef: ActorRef =
    system.actorOf(UserRegistryActor.props, "userRegistryActor")

  override def userRoutesUsingActors: AbstractUserRoutes =
    new AbstractUserRoutes(new UserServiceUsingActorRegister()) {}

  override protected def beforeAll(): Unit = {
    val cypher = Source
      .fromURL(getClass.getResource("/preload-graph.cypher"))
      .mkString

    InMemoryNeo4j.run(cypher, Map())
  }

  override def userRoutesUsingDB: AbstractUserRoutes =
    new AbstractUserRoutes(new UserServiceUsingDB(new UserRepository())) {}
}
