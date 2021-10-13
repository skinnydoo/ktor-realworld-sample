package io.skinnydoo.common

typealias UserNotFound = NotFoundError.UserNotFound
typealias UserExists = AlreadyExistsError.UserAlreadyExist

sealed class LoginError {
  object EmailUnknown : LoginError()
  object PasswordInvalid : LoginError()
}

sealed class AlreadyExistsError {
  data class UserAlreadyExist(val message: String = "User exists") : AlreadyExistsError()
}

sealed class NotFoundError {
  data class UserNotFound(val message: String = "User not found") : NotFoundError()
}

sealed class InvalidPropertyError {
  data class EmailInvalid(val message: String = "Email invalid") : InvalidPropertyError()
}

/*private fun format(base: String, message: String?): String = buildString {
  append(base)
  if (!message.isNullOrEmpty()) {
    append(": ")
    append(message)
  }
}*/
