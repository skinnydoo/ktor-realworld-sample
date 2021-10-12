package io.skinnydoo.users.auth

import io.skinnydoo.users.UserTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object AccessTokenTable : LongIdTable("access_tokens") {
  val userId = reference(
    "user_id",
    UserTable,
    onDelete = ReferenceOption.CASCADE,
    onUpdate = ReferenceOption.CASCADE
  ).uniqueIndex()
  val token = varchar("token", 255).uniqueIndex()
}
