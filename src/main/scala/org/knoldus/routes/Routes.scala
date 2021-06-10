package org.knoldus.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.knoldus.jwt.JWTGenerator
import org.knoldus.model._
import org.knoldus.service.UserService

import java.util.UUID.randomUUID
import scala.util.{Failure, Success}

class Routes(userService : UserService, jwtGenerator: JWTGenerator) extends UserJsonProtocol with SprayJsonSupport {


  //val userService = new UserService(new UserDatabase)

  val userManagementRouteSkeleton: Route =
    pathPrefix("api" / "user") {
      get {
        path("getAllUsers") {
          optionalHeaderValueByName("Authorization") {
            case Some(token) =>
              if (jwtGenerator.isTokenValid(token)) {
                if (jwtGenerator.isTokenExpired(token)) {
                  complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "Token expired."))
                } else {
                  val users = userService.getAllUser
                  complete(users)
                }
              } else {
                complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "Token is invalid, or has been tampered with."))
              }
            case _ => complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "No token provided!"))

          }
        } ~
          (path(Segment) | parameter(Symbol("id"))) { id =>
            optionalHeaderValueByName("Authorization") {
              case Some(token) =>
                if (jwtGenerator.isTokenValid(token)) {
                  if (jwtGenerator.isTokenExpired(token)) {
                    complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "Token expired."))
                  } else {
                    val user = userService.getById(Some(id))
                    complete(user)
                  }
                } else {
                  complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "Token is invalid, or has been tampered with."))
                }
              case _ => complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "No token provided!"))

            }
          }
      } ~
        post {
          path("register") {
            entity(as[User]) {
              user =>
                if(user.id.isEmpty) {
                  val newUser = user.copy(id = Some(randomUUID().toString), reward = Some(0), status = Some(1))
                  println(newUser)
                  onComplete(userService.createNewUser(newUser)) {
                    case Success(value) =>
                      if (value) complete("User is registered successfully")
                      else complete(StatusCodes.BadRequest, "User is not registered successfully")
                    case Failure(ex) => complete(StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}")
                  }
                }
                else{
                  onComplete(userService.createNewUser(user)) {
                    case Success(value) =>
                      if (value) complete("User is registered successfully")
                      else complete(StatusCodes.BadRequest, "User is not registered successfully")
                    case Failure(ex) => complete(StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}")
                  }
                }
            }
          } ~
            path("login") {
              entity(as[UserCredentials]) {
                credentials =>
                  onComplete(userService.login(credentials)) {
                    case Success(value) =>
                      value match {
                        case Some(result) =>
                          respondWithHeader(RawHeader("Access-Token", result)) {
                            complete(StatusCodes.OK, s"login is successful take token to move further")
                          }
                        case None => complete(StatusCodes.BadRequest, "The credentials are not correct")
                      }
                    case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
                  }
              }
            } ~
            path("create" / "moderator") {
              entity(as[UserId]) {
                userID =>
                  optionalHeaderValueByName("Authorization") {
                    case Some(token) =>
                      if (jwtGenerator.isTokenValid(token)) {
                        if (jwtGenerator.isTokenExpired(token)) {
                          complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "Token expired."))
                        } else {
                          onComplete(userService.createModerator(userID.id)) {
                            case Success(value) =>
                              if (value) complete(StatusCodes.OK, s"User's role is set as Moderator")
                              else complete(StatusCodes.InternalServerError, s"User's role can't set as Moderator")
                            case Failure(exception) => complete(StatusCodes.BadRequest, s"Can't perform action because ${exception.getMessage}")
                          }
                        }
                      } else {
                        complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "Token is invalid, or has been tampered with."))
                      }
                    case _ => complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "No token provided!"))

                  }
              }
            } ~
            path("disable") {
              entity(as[UserId]) {
                userID =>
                  optionalHeaderValueByName("Authorization") {
                    case Some(token) =>
                      if (jwtGenerator.isTokenValid(token)) {
                        if (jwtGenerator.isTokenExpired(token)) {
                          complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "Token expired."))
                        } else {
                          onComplete(userService.disableUser(userID.id)) {
                            case Success(value) =>
                              if (value) complete(StatusCodes.OK, s"User is disabled successfully")
                              else complete(StatusCodes.InternalServerError, s"User is not disabled")
                            case Failure(exception) => complete(StatusCodes.BadRequest, s"Can't perform action because ${exception.getMessage}")
                          }
                        }
                      } else {
                        complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "Token is invalid, or has been tampered with."))
                      }
                    case _ => complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "No token provided!"))
                  }
              }
            } ~
            path("enable") {
              entity(as[UserId]) {
                userID =>
                  optionalHeaderValueByName("Authorization") {
                    case Some(token) =>
                      if (jwtGenerator.isTokenValid(token)) {
                        if (jwtGenerator.isTokenExpired(token)) {
                          complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "Token expired."))
                        } else {
                          onComplete(userService.enableUser(userID.id)) {
                            case Success(value) =>
                              if (value) complete(StatusCodes.OK, s"User is enabled successfully")
                              else complete(StatusCodes.InternalServerError, s"User is not enabled")
                            case Failure(exception) => complete(StatusCodes.BadRequest, s"Can't perform action because ${exception.getMessage}")
                          }
                        }
                      } else {
                        complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "Token is invalid, or has been tampered with."))
                      }
                    case _ => complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "No token provided!"))
                  }
              }
            } ~
            path("update") {
              entity(as[List[User]]) { users =>
                optionalHeaderValueByName("Authorization") {
                  case Some(token) =>
                    if (jwtGenerator.isTokenValid(token)) {
                      if (jwtGenerator.isTokenExpired(token)) {
                        complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "Token expired."))
                      } else {
                        if (users.head.id.isEmpty || users(1).id.isEmpty) {
                          complete(StatusCodes.BadRequest)
                        }
                        else {
                          onComplete(userService.updateUser(users.head, users(1))) {
                            case Success(value) =>
                              if (value) complete("The user is updated successfully")
                              else complete("The user is not updated successfully")
                            case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
                          }
                        }
                      }
                    } else {
                      complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "Token is invalid, or has been tampered with."))
                    }
                  case _ => complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "No token provided!"))
                }
              }
            }
        }~
        delete {
          path("delete"){
            entity(as[User]) { user =>
              optionalHeaderValueByName("Authorization") {
                case Some(token) =>
                  if (jwtGenerator.isTokenValid(token)) {
                    if (jwtGenerator.isTokenExpired(token)) {
                      complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "Token expired."))
                    } else {
                      if(user.id.isEmpty) complete(InternalServerError, s"please provide the id of the user")
                      else{
                        onComplete(userService.deleteUser(user)){
                          case Success(value) =>
                            if(value) complete("The user is deleted successfully")
                            else complete("The user is not deleted")
                          case Failure(ex)    => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
                        }
                      }
                    }
                  } else {
                    complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "Token is invalid, or has been tampered with."))
                  }
                case _ => complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "No token provided!"))
              }
            }
          }
        }
    }
}
