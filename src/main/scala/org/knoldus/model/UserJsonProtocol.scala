package org.knoldus.model

import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, RootJsonFormat}

trait UserJsonProtocol extends DefaultJsonProtocol {

  implicit object UserTypeJsonFormat extends JsonFormat[UserType.Value] {
    def write(obj: UserType.Value): JsValue = JsString(obj.toString)
    def read(json: JsValue): UserType.Value = json match {
      case JsString(str) => UserType.withName(str)
      case _             => throw DeserializationException("Enum string expected")
    }
  }


  implicit val userFormat: RootJsonFormat[User] = jsonFormat7(User)
  implicit val loginFormat : RootJsonFormat[UserCredentials] = jsonFormat2(UserCredentials)
  implicit val userRoleFormat: RootJsonFormat[UserRole] = jsonFormat1(UserRole)
  implicit val userIdFormat : RootJsonFormat[UserId] = jsonFormat1(UserId)
}