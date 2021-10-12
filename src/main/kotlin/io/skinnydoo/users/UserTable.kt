package io.skinnydoo.users

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column

object UserTable : UUIDTable(name = "users") {
  val username: Column<String> = varchar("username", 100).uniqueIndex()
  val email = varchar("email", 128).uniqueIndex()
  val password = varchar("password", 255)
  val bio = varchar("bio", 128).default("")
  val image = varchar("image", 180).nullable()
}
