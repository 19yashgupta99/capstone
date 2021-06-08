package org.knoldus.model

import org.knoldus.model.UserType.UserType



  case class User(
                   id       : Option[String],
                   name     : String,
                   username : String,
                   password : String,
                   category : UserType,
                   reward   : Option[Int],
                   status   : Option[Int]
                 )

