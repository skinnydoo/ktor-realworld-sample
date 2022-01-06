package io.skinnydoo

import io.kotest.assertions.*
import io.kotest.assertions.json.*
import io.kotest.assertions.ktor.*
import io.kotest.core.spec.style.*
import io.kotest.matchers.*
import io.kotest.matchers.maps.*
import io.kotest.matchers.nulls.*
import io.kotest.matchers.string.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.server.testing.*
import io.skinnydoo.common.Email
import io.skinnydoo.common.Password
import io.skinnydoo.common.Username
import io.skinnydoo.users.*
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import java.time.LocalDateTime

class ServerTest : DescribeSpec({
  context("Authentication") {
    val username = Username("tu${LocalDateTime.now().nano}")
    val email = Email("$username@email.com")
    val password = Password("test123!")
    var apiToken = ""

    describe("Register") {
      describe("POST /api/v1/users") {
        it("should return a response containing the user") {
          withTestServer {
            val request = RegisterUserRequest(NewUserCredentials(username, email, password))
            val path = API_V1 + application.locations.href(UserCreateRoute())
            val call = handleRequest(HttpMethod.Post, uri = path) {
              addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
              setBody(json.encodeToString(RegisterUserRequest.serializer(), request))
            }

            val response = call.response
            val content = response.content
            assertSoftly {
              response shouldHaveStatus HttpStatusCode.Created
              response should haveContentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))

              content shouldNot beNull()
              content shouldNot beBlank()

              val responseJson = json.decodeFromString(JsonObject.serializer(), content.orEmpty().ifEmpty { "{}" })
              responseJson shouldContainKey "user"

              val user = responseJson.getValue("user").jsonObject

              user shouldContainKey "email"
              user shouldContainKey "username"
              user shouldContainKey "bio"
              user shouldContainKey "image"
              user shouldContainKey "token"
            }
          }
        }
      }
    }

    describe("Login") {
      describe("POST /api/v1/users/login") {
        it("should return a response containing the user") {
          withTestServer {
            val request = UserLoginRequest(UserLoginCredentials(email, password))
            val path = API_V1 + application.locations.href(UserLoginRoute())
            val call = handleRequest(HttpMethod.Post, path) {
              addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
              setBody(json.encodeToString(UserLoginRequest.serializer(), request))
            }

            assertSoftly(call.response) {
              this shouldHaveStatus HttpStatusCode.Created
              this should haveContentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))
              content shouldContainJsonKey "user"
              content shouldContainJsonKey "user.email"
              content shouldContainJsonKey "user.username"
              content shouldContainJsonKey "user.bio"
              content shouldContainJsonKey "user.token"
              content should containJsonKeyValue("user.image", null as String?)
            }
          }
        }
      }
    }

    describe("Login and Remember Token") {
      describe("POST /api/v1/users/login") {
        it("should return a response containing the user") {
          withTestServer {
            val request = UserLoginRequest(UserLoginCredentials(email, password))
            val path = API_V1 + application.locations.href(UserLoginRoute())
            val call = handleRequest(HttpMethod.Post, path) {
              addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
              setBody(json.encodeToString(UserLoginRequest.serializer(), request))
            }

            assertSoftly(call.response) {
              this shouldHaveStatus HttpStatusCode.Created
              this should haveContentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))
              content shouldContainJsonKey "user"
              content shouldContainJsonKey "user.email"
              content shouldContainJsonKey "user.username"
              content shouldContainJsonKey "user.bio"
              content shouldContainJsonKey "user.token"
              content should containJsonKeyValue("user.image", null as String?)
            }

            val content = call.response.content
            val response =
              json.decodeFromString(UserResponse.serializer().nullable, content.orEmpty()) ?: return@withTestServer
            apiToken = response.user.token

            apiToken shouldNot beBlank()
            apiToken shouldBe response.user.token
          }
        }
      }
    }

    describe("Current User") {
      describe("GET /api/v1/user") {
        it("should return the current user") {
          withTestServer {
            val path = API_V1 + application.locations.href(UserRoute())
            val call = handleRequest(HttpMethod.Get, path) {
              addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
              addHeader(HttpHeaders.Authorization, "Token $apiToken")
            }

            assertSoftly(call.response) {
              this shouldHaveStatus HttpStatusCode.OK
              this should haveContentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))
              content shouldContainJsonKey "user"
              content shouldContainJsonKey "user.email"
              content shouldContainJsonKey "user.username"
              content shouldContainJsonKey "user.bio"
              content shouldContainJsonKey "user.token"
              content should containJsonKeyValue("user.image", null as String?)
            }
          }
        }
      }
    }
  }
})
