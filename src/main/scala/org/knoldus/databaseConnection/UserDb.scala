package org.knoldus.databaseConnection

import org.knoldus.jwt.JWTGenerator
import org.knoldus.model.UserType.{Moderator, UserType}
import org.knoldus.model.{User, UserCredentials, UserType}
import slick.ast.BaseTypedType
import slick.jdbc.MySQLProfile.api._
import slick.jdbc.{JdbcType, MySQLProfile}
import slick.lifted.{ProvenShape, Tag}

import scala.concurrent.{ExecutionContext, Future}
import scala.math.Ordered.orderingToOrdered

class UserDBSchema(tag: Tag) extends Table[User](tag, "capstonetable") {

  implicit val enumMapper: JdbcType[UserType] with BaseTypedType[UserType] = MappedColumnType.base[UserType ,String](
    category => category.toString,
    string => UserType.withName(string)
  )

  def id : Rep[Option[String]] = column[Option[String]]("userid")
  def name: Rep[String] = column[String]("name")
  def username : Rep[String] = column[String]("username")
  def password : Rep[String] = column[String]("password")
  def category : Rep[UserType] = column[UserType]("usertype")
  def reward   : Rep[Option[Int]] = column[Option[Int]]("reward")
  def status : Rep[Option[Int]] = column[Option[Int]]("status")

  def * : ProvenShape[User] = (id,name,username,password,category,reward,status) <> (User.tupled, User.unapply)
}

class UserDb(db: MySQLProfile.backend.DatabaseDef)(implicit ec: ExecutionContext)
  extends TableQuery(new UserDBSchema(_)){

  implicit val enumMapper: JdbcType[UserType] with BaseTypedType[UserType.Value] = MappedColumnType.base[UserType ,String](
    category => category.toString,
    string => UserType.withName(string)
  )

  val jwtGenerator: JWTGenerator = new JWTGenerator

  def getById(id : Option[String]) : Future[Seq[User]] = {
    db.run[Seq[User]](this.filter(_.id === id).result)
  }

  def insert(user: User) : Future[Int] = {
    db.run(this += user)
  }

  def login(credentials : UserCredentials) : Future[Option[String]] ={
    for {
      user1 <- db.run[Seq[User]](this.filter(_.username === credentials.username).result)
    }yield {
      if(user1.nonEmpty){
        if(user1.head.status > Some(0)){
          if(user1.head.password == credentials.password){
            if(user1.head.category != UserType.Admin && user1.head.category != Moderator){
              if(user1.head.category == UserType.Premium) {
                updateReward(user1.head.id,20)
                Some(jwtGenerator.createToken("Premium" , 60))
              }
              else {
                updateReward(user1.head.id,10)
                Some(jwtGenerator.createToken(user1.head.category.toString , 60))
              }
            }
            else Some(jwtGenerator.createToken("Admin" , 60))
          }
          else None
        }
        else None
      }
      else None
    }
  }

  def createModerator(id : String) : Future[Int] ={
    db.run(this.filter(_.id === id).map(_.category).update(Moderator))
  }

  def updateReward(id : Option[String], reward : Int):Future[Boolean] = {
    val a = for{
      user <- db.run[Seq[User]](this.filter(_.id === id).result)
    }yield {
      val result = db.run(this.filter(_.id === id).map(_.reward).update(user.head.reward.flatMap(newReward => Some(reward + newReward))))
      result.flatMap{
        value =>
          if(value > 0) Future.successful(true)
          else Future.successful(false)
      }
    }
    a.flatMap(x => x)
  }

  def disableUser(id : String): Future[Int] = {
    db.run(this.filter(_.id === id).map(_.status).update(Some(0)))
  }

  def enableUser(id : String): Future[Int] = {
    db.run(this.filter(_.id === id).map(_.status).update(Some(1)))
  }

  def getAll : Future[Seq[User]] = {
    db.run(this.result)
  }

  def delete(user : User): Future[Int] ={
    db.run(this.filter(_.id === user.id).delete)
  }

  def updateUser(oldUser : User , newUser : User) : Future[Int] ={
    val result = delete(oldUser)
    val finalResult : Future[Int] = result.flatMap[Int]{
      value => {
        if (value > 0) insert(newUser)
        else Future.successful(0)
      }
    }
    finalResult
  }

  def updateUserName(user : User, name :String): Future[Int] = {
    db.run(this.filter(_.id === user.id).map(_.name).update(name))
  }

  def updateUserCategory(user:User , newCategory : UserType.Value): Future[Int] = {
    db.run(this.filter(_.id === user.id).map(_.category).update(newCategory))
  }

}

