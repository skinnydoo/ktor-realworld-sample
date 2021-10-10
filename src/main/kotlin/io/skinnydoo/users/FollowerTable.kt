package io.skinnydoo.users

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption.CASCADE
import org.jetbrains.exposed.sql.Table
import java.util.UUID

object FollowerTable : Table(name = "user_followers") {
  val userId: Column<EntityID<UUID>> = reference("user_id", UserTable, onDelete = CASCADE)
  val followerId = reference("follower_id", UserTable, onDelete = CASCADE)
}
