package models.entities

import java.time.LocalDateTime

case class Role(id: Option[Int] = None, name: String)

case class User(
                 id: Option[Int] = None,
                 name: String,
                 email: String,
                 password: String,
                 avatarUrl: Option[String] = None,
                 roleId: Option[Int] = None,
                 createdAt: Option[LocalDateTime] = None,
                 updatedAt: Option[LocalDateTime] = None
               )