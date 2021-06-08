package org.knoldus.databaseCalls

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import org.knoldus.dao.Dao
import org.knoldus.databaseConnection.{DBConnection, UserDb}
import org.knoldus.model.{User, UserCredentials, UserId, UserType}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}


case class DisableUser(userID : String)
case class EnableUser(userId: String)
case class CreateModerator(userID : String)
case class LoginRequest(credentials: UserCredentials)
case class CreateUser(user:User)
case object ListAllUsers
case class UpdateUser(oldUser : User, newUser : User)
case class UpdateUserName(user: User, newName: String)
case class UpdateUserCategory(user: User, newCategory: UserType.Value)
case class DeleteUser(user: User)
case class GetUserByID(id: Option[String])

class ActorModel extends Actor{

  val dbConnection = new DBConnection
  val userDbObject = new UserDb(dbConnection.db)

  override def receive: Receive = crudOperations()

  def crudOperations() :Receive ={
    case CreateUser(user) =>
      val res = Future{
          Try{
            val result = userDbObject.insert(user)
            result.map{
              value => {
                if(value > 0) Success
                else Failure
              }
            }
          } match {
            case Failure(_) => false
            case Success(_) => true
          }
      }.recover{
        case _:RuntimeException => false
      }
      res.pipeTo(sender())

    case CreateModerator(id) =>
      val res = Future{
        Try{
          val result = userDbObject.createModerator(id)
          result.map{
            value =>{
              if(value > 0) Success
              else Failure
            }
          }
        } match {
          case Success(_) => true
          case Failure(_) => false
        }
      }.recover{
        case _:RuntimeException => false
      }
      res.pipeTo(sender())

    case DisableUser(id) =>
      val res = Future{
        Try{
          val result = userDbObject.disableUser(id)
          result.map{
            value =>{
              if(value > 0) Success
              else Failure
            }
          }
        } match {
          case Success(_) => true
          case Failure(_) => false
        }
      }.recover{
        case _:RuntimeException => false
      }
      res.pipeTo(sender())

    case EnableUser(id) =>
      val res = Future{
        Try{
          val result = userDbObject.enableUser(id)
          result.map{
            value =>{
              if(value > 0) Success
              else Failure
            }
          }
        } match {
          case Success(_) => true
          case Failure(_) => false
        }
      }.recover{
        case _:RuntimeException => false
      }
      res.pipeTo(sender())

    case LoginRequest(credentials) =>
      val result = {
        userDbObject.login(credentials)
      }.recover{
        case _:RuntimeException => None
      }
      result.onComplete{
        case Success(value) => println("Jwt Token = "+value)
      }
      result.pipeTo(sender())

    case ListAllUsers =>
        val result = {
          userDbObject.getAll
        }.recover{
          case _:RuntimeException => List()
        }
        val finalResult =result.map{
          seq =>
            seq.toList
        }.recover{
          case _:RuntimeException => List()
        }
      finalResult.pipeTo(sender())

    case UpdateUser(oldUser,newUser) =>
      val res =  Future{
        Try{
          val result = userDbObject.updateUser(oldUser,newUser)
          result.map{
            value =>{
              if(value > 0) Success
              else Failure
            }
          }
        } match {
            case Failure(_) => false
            case Success(_) => true
          }
      }.recover{
        case _:RuntimeException => false
      }
      res.pipeTo(sender())

    case UpdateUserName(user,newName) =>
      val res = Future{
        Try{
          val result = userDbObject.updateUserName(user,newName)
          result.map{
            value =>{
              if(value > 0) Success
              else Failure
            }
          }
        } match {
            case Success(_) => true
            case Failure(_) => false
          }
      }.recover{
        case _:RuntimeException => false
      }
      res.pipeTo(sender())

    case UpdateUserCategory(user, newCategory) =>
      val res = Future{
        Try{
          val result = userDbObject.updateUserCategory(user,newCategory)
          result.map{
            value =>{
              if(value > 0) Success
              else Failure
            }
          }
        } match {
            case Success(_) => true
            case Failure(_) => false
          }
      }.recover{
        case _:RuntimeException => false
      }
      res.pipeTo(sender())

    case DeleteUser(user) =>
      val res = Future{
        Try{
          val result = userDbObject.delete(user)
          result.map{
            value =>{
              if(value > 0) Success
              else Failure
            }
          }
        } match {
          case Success(_) => true
          case Failure(_) =>false
        }
      }.recover{
        case _:RuntimeException => false
      }
      res.pipeTo(sender())

    case GetUserByID(id) =>
      val result = {
        userDbObject.getById(id)
      }.recover{
        case _:RuntimeException => List()
      }
      val secondResult =result.map{
        seq =>
          seq.toList
      }.recover{
        case _:RuntimeException => List()
      }
      val finalResult = secondResult.map{
        user => user.head
      }
      finalResult.pipeTo(sender())

  }
}

class UserDatabase extends Dao[User]{

  val system: ActorSystem = ActorSystem("System")
  val actor: ActorRef = system.actorOf(Props[ActorModel])
  implicit val timeout: Timeout = Timeout(5 seconds)

  override def createUser(user: User): Future[Boolean] =
    (actor ? CreateUser(user)).mapTo[Boolean]

  override def listAllUser(): Future[List[User]] =
    (actor ? ListAllUsers).mapTo[List[User]]

  override def updateUser(oldUser : User, newUser : User): Future[Boolean] =
    (actor ? UpdateUser(oldUser,newUser)).mapTo[Boolean]

  override def updateUserName(user: User, newName: String): Future[Boolean] =
    (actor ? UpdateUserName(user,newName)).mapTo[Boolean]

  override def updateUserCategory(user: User, newCategory: UserType.Value): Future[Boolean] =
    (actor ? UpdateUserCategory(user, newCategory)).mapTo[Boolean]

  override def deleteUser(user: User): Future[Boolean] =
    (actor ? DeleteUser(user)).mapTo[Boolean]

  override def getUserById(id: Option[String]): Future[User] =
    (actor ? GetUserByID(id)).mapTo[User]

  override def login(credentials: UserCredentials): Future[Option[String]] =
    (actor ? LoginRequest(credentials)).mapTo[Option[String]]

  override def createModerator(userId: String): Future[Boolean] =
    (actor ? CreateModerator(userId)).mapTo[Boolean]

  override def disableUser(userID: String): Future[Boolean] =
    (actor ? DisableUser(userID)).mapTo[Boolean]

  override def enableUser(userID: String): Future[Boolean] =
    (actor ? EnableUser(userID)).mapTo[Boolean]
}
