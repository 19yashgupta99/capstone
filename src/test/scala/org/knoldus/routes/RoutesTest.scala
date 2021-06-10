package org.knoldus.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.knoldus.jwt.JWTGenerator
import org.knoldus.model.UserType._
import org.knoldus.model.{User, UserCredentials, UserId, UserJsonProtocol}
import org.knoldus.service.UserService
import org.mockito.Mockito.{mock, when}
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

import java.util.UUID.randomUUID
import scala.concurrent.Future

class RoutesTest extends AsyncFlatSpec with Matchers with ScalatestRouteTest with UserJsonProtocol with SprayJsonSupport{

  val mockedUserService: UserService = mock(classOf[UserService])
  val mockedJWTGenerator: JWTGenerator = mock(classOf[JWTGenerator])
  val route = new Routes(mockedUserService, mockedJWTGenerator)
  val user: User = User(Some(randomUUID().toString),"Yash Gupta","yash_gupta","password",Admin,Some(0),Some(1))
  val token: String = "sjdhjkshkshkdjksshdjkshdkjsshdk.dsjhdjksdsd.sdssgdjsgdsghd"

  "The route" should "return all the user" in{
    when(mockedJWTGenerator.isTokenValid(token)) thenReturn true
    when(mockedJWTGenerator.isTokenExpired(token)) thenReturn false
    when(mockedUserService.getAllUser) thenReturn Future.successful(List(user))
    Get("/api/user/getAllUsers") ~> addHeader("Authorization", token) ~> route.userManagementRouteSkeleton ~> check{
      status shouldBe StatusCodes.OK
      responseAs[List[User]] shouldBe List(user)
    }
  }

  "The route" should "return the user with the given ID" in{
    when(mockedJWTGenerator.isTokenValid(token)) thenReturn true
    when(mockedJWTGenerator.isTokenExpired(token)) thenReturn false
    when(mockedUserService.getById(Some("abfba0fa-beef-4db9-b19a-78f8b7cdfacc"))) thenReturn Future.successful(user)
    Get("/api/user/abfba0fa-beef-4db9-b19a-78f8b7cdfacc") ~> addHeader("Authorization", token) ~> route.userManagementRouteSkeleton ~> check{
      status shouldBe StatusCodes.OK
      responseAs[User] shouldBe user
    }
  }

  "The route" should "register the new user in database" in{
    when(mockedUserService.createNewUser(user)) thenReturn Future.successful(true)
    Post("/api/user/register", user) ~> route.userManagementRouteSkeleton ~> check{
      status shouldBe StatusCodes.OK
      responseAs[String] shouldBe "User is registered successfully"
    }
  }

  "The route" should "genrate the tokin for the Login request" in{
    val loginCredentials = UserCredentials("yash_gupta","password")
    when(mockedUserService.login(loginCredentials)) thenReturn Future.successful(Some(token))
    Post("/api/user/login",loginCredentials) ~> route.userManagementRouteSkeleton ~> check{
      header("Access-Token") shouldEqual Some(RawHeader("Access-Token",token))
      status shouldBe StatusCodes.OK
    }
  }

  "The route" should "create the user as moderator" in{
    val userId : UserId = UserId(id = user.id.head)
    when(mockedJWTGenerator.isTokenValid(token)) thenReturn true
    when(mockedJWTGenerator.isTokenExpired(token)) thenReturn false
    when(mockedUserService.createModerator(user.id.head)) thenReturn Future.successful(true)
    Post("/api/user/create/moderator", userId) ~> addHeader("Authorization", token) ~> route.userManagementRouteSkeleton ~> check{
      status shouldBe StatusCodes.OK
      responseAs[String] shouldBe "User's role is set as Moderator"
    }
  }

  "The route" should "disable the user r" in{
    val userId : UserId = UserId(id = user.id.head)
    when(mockedJWTGenerator.isTokenValid(token)) thenReturn true
    when(mockedJWTGenerator.isTokenExpired(token)) thenReturn false
    when(mockedUserService.disableUser(user.id.head)) thenReturn Future.successful(true)
    Post("/api/user/disable", userId) ~> addHeader("Authorization", token) ~> route.userManagementRouteSkeleton ~> check{
      status shouldBe StatusCodes.OK
      responseAs[String] shouldBe "User is disabled successfully"
    }
  }

  "The route" should "enable the user r" in{
    val userId : UserId = UserId(id = user.id.head)
    when(mockedJWTGenerator.isTokenValid(token)) thenReturn true
    when(mockedJWTGenerator.isTokenExpired(token)) thenReturn false
    when(mockedUserService.enableUser(user.id.head)) thenReturn Future.successful(true)
    Post("/api/user/enable", userId) ~> addHeader("Authorization", token) ~> route.userManagementRouteSkeleton ~> check{
      status shouldBe StatusCodes.OK
      responseAs[String] shouldBe "User is enabled successfully"
    }
  }

  "The route" should "update the existing user with new user" in{
    val newUser:User = User(Some(randomUUID().toString),"Rudra Gupta","rudra_gupta", "password1" , Customer,Some(0),Some(1))
    when(mockedJWTGenerator.isTokenValid(token)) thenReturn true
    when(mockedJWTGenerator.isTokenExpired(token)) thenReturn false
    when(mockedUserService.updateUser(user,newUser)) thenReturn Future.successful(true)
    Post("/api/user/update", List(user,newUser)) ~> addHeader("Authorization", token) ~> route.userManagementRouteSkeleton ~> check{
      status shouldBe StatusCodes.OK
      responseAs[String] shouldBe "The user is updated successfully"
    }
  }

  "The route" should "delete the existing user" in{
    when(mockedJWTGenerator.isTokenValid(token)) thenReturn true
    when(mockedJWTGenerator.isTokenExpired(token)) thenReturn false
    when(mockedUserService.deleteUser(user)) thenReturn Future.successful(true)
    Delete("/api/user/delete", user) ~> addHeader("Authorization", token) ~> route.userManagementRouteSkeleton ~> check{
      status shouldBe StatusCodes.OK
      responseAs[String] shouldBe "The user is deleted successfully"
    }
  }


}
