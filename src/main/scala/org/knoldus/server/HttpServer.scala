package org.knoldus.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import org.knoldus.databaseCalls.UserDatabase
import org.knoldus.jwt.JWTGenerator
import org.knoldus.routes.Routes
import org.knoldus.service.UserService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object HttpServer extends App {
  implicit val system: ActorSystem = ActorSystem("MarshallingJSON")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val routes = new Routes(new UserService(new UserDatabase),new JWTGenerator)

  val server = Http().newServerAt("localhost", 8080).bindFlow(routes.userManagementRouteSkeleton)
  server.onComplete{
    case Success(value) => println(s"Now server is running at ${value.localAddress}")
    case Failure(exception) => println(s"${exception}")
  }

}
