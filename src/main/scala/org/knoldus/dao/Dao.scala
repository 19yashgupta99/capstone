package org.knoldus.dao

import org.knoldus.model.{User, UserCredentials, UserId, UserType}

import scala.concurrent.Future

trait Dao[T] {

  def createUser(obj : T) : Future[Boolean]

  def login(credentials: UserCredentials) : Future[Option[String]]

  def createModerator(userId : String) : Future[Boolean]

  def disableUser(userID : String) : Future[Boolean]

  def enableUser(userID : String) : Future[Boolean]

  def listAllUser():Future[List[T]]

  def updateUser(oldObject: T, newObject:T): Future[Boolean]

  def deleteUser(obj : T) : Future[Boolean]

  def updateUserName(obj : T , newName : String) : Future[Boolean]

  def updateUserCategory(obj :T , newCategory :UserType.Value) : Future[Boolean]

  def getUserById(id: Option[String]) :Future[User]

}
