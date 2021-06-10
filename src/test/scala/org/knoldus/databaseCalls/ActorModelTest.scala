package org.knoldus.databaseCalls
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import org.knoldus.databaseCalls.ActorModelMessages._
import org.knoldus.databaseConnection.UserDb
import org.knoldus.model.{User, UserType}
import org.mockito.Mockito.{mock, when}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.PatienceConfiguration.{Timeout => wert}
import org.scalatest.concurrent.ScalaFutures.whenReady
import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import java.util.UUID.randomUUID
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class ActorModelTest extends TestKit(ActorSystem("ActorModelSpec"))
  with ImplicitSender
  with AsyncFlatSpecLike
  with BeforeAndAfterAll{

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val mockedUserDB     : UserDb = mock(classOf[UserDb])

  val user: User = User(Some(randomUUID().toString),"Yash Gupta","YashGupta123","password",UserType.Admin,Some(0),Some(1))
  implicit val timeout: Timeout = Timeout(5 seconds)
  implicit val timeout1: wert = wert(5 seconds)

  val actor: ActorRef = system.actorOf(Props(new ActorModel(mockedUserDB)))

  "The ActorModel " should "create a User in the database" in {
    when(mockedUserDB.insert(user)) thenReturn Future.successful(1)
    whenReady((actor ? CreateUser(user)).mapTo[Boolean],timeout1){
      result => result shouldBe true
    }
  }

  "The ActorModel " should "create the User as moderator in the database" in {
    when(mockedUserDB.createModerator(user.id.head)) thenReturn Future.successful(1)
    whenReady((actor ? CreateModerator(user.id.head)).mapTo[Boolean],timeout1){
      result => result shouldBe true
    }
  }

  "The ActorModel" should "List all users" in{
    when(mockedUserDB.getAll) thenReturn Future.successful(Seq.empty[User])
    whenReady((actor ? ListAllUsers).mapTo[List[User]]){
      result => result shouldBe List()
    }
  }

  "The ActorModel" should "Update the user with new User" in{
    val newUser = User(Some(randomUUID().toString),"Nitesh","nitesh123","password2",UserType.Customer,Some(0),Some(1))
    when(mockedUserDB.updateUser(user,newUser)) thenReturn Future.successful(1)
    whenReady((actor ? UpdateUser(user,newUser)).mapTo[Boolean]){
      result => result shouldBe true
    }
  }

  "The ActorModel " should "update the username of existing user" in{
    when(mockedUserDB.updateUserName(user,"yash_gupta")) thenReturn Future.successful(1)
    whenReady((actor ? UpdateUserName(user,"yash_gupta")).mapTo[Boolean]){
      result => result shouldBe true
    }
  }

  "The ActorModel" should "update the category of existing user" in{
    when(mockedUserDB.updateUserCategory(user,UserType.Customer)) thenReturn Future.successful(1)
    whenReady((actor ? UpdateUserCategory(user, UserType.Customer)).mapTo[Boolean]){
      result => result shouldBe true
    }
  }

  "The ActorModel " should "disable the User in the database" in {
    when(mockedUserDB.disableUser(user.id.head)) thenReturn Future.successful(1)
    whenReady((actor ? DisableUser(user.id.head)).mapTo[Boolean],timeout1){
      result => result shouldBe true
    }
  }

  "The ActorModel " should "enable the User in the database" in {
    when(mockedUserDB.enableUser(user.id.head)) thenReturn Future.successful(1)
    whenReady((actor ? EnableUser(user.id.head)).mapTo[Boolean],timeout1){
      result => result shouldBe true
    }
  }

  "The ActorModel" should "delete the user" in {
    when(mockedUserDB.delete(user)) thenReturn Future.successful(1)
    whenReady((actor ? DeleteUser(user)).mapTo[Boolean]){
      result => result shouldBe true
    }
  }

  "The ActorModel" should "get the user b the id" in{
    when(mockedUserDB.getById(user.id)) thenReturn Future.successful(Seq(user))
    whenReady((actor ? GetUserByID(user.id)).mapTo[User]){
      result => result shouldBe user
    }
  }


}