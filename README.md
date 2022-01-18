# ![Ktor RealWorld Sample App](https://github.com/gothinkster/realworld-starter-kit/raw/master/logo.png)

> ### Ktor codebase written in a functional style containing real world examples (CRUD, auth, advanced patterns, etc) that adheres to the [RealWorld](https://realworld-docs.netlify.app/docs/specs/backend-specs/introduction) spec and API.

## [RealWorld](https://realworld-docs.netlify.app)

This codebase was created to familiarize myself with Ktor, as well as the [graphql-kotlin](https://opensource.expediagroup.com/graphql-kotlin/docs/) library. It includes CRUD operations, authentication, routing, pagination, and more.


For more information on how to this works with other frontends/backends, head over to the [RealWorld](https://github.com/gothinkster/realworld) repo.

# Overview

## What tools & frameworks are we using?

- [**Kotlin 1.6**](https://kotlinlang.org) - our language of choice
- [**Gradle (with Kotlin DSL)**](https://github.com/gradle/gradle) - our build tool of choice
- [**Ktor**](https://github.com/ktorio/ktor) - for creating web application with Kotlin
- [**graphql-kotlin**](https://github.com/ExpediaGroup/graphql-kotlin) - for running GraphQL with Kotlin
- [**Λrrow**](https://github.com/arrow-kt/arrow) - Functional compagnion to Kotlin
- [**Koin**](https://github.com/InsertKoinIO/koin) - our dependency injection library
- [**kotlinx.serialization**](https://github.com/Kotlin/kotlinx.serialization) - for JSON serialization/deserialiation
- [**Exposed**](https://github.com/JetBrains/Exposed) - Kotlin SQL framework for database access
- [**HikariCP**](https://github.com/brettwooldridge/HikariCP) - high-performance JDBC connection pool
- [**H2**](https://www.h2database.com/html/main.html) - a lightweight in-memory database used for testing
- [**kotest**](https://github.com/kotest/kotest/) - as testing framework for kotlin.

## Architecture Overview

The application uses a **package-by-feature** structure with the following packages:
1. `common`: contains technical configuration classes (e.g. for DI, database connection, connection pooling, logging, etc)
2. `users`: contains user related routes, domain models, entities and business logic
3. `articles`: contains article related routes, domain models, types and business logic
4. `profiles`: contains user profiles routes, domain models and business logic

# Getting started

## Prerequisites

#### MySQL

If you have a local MySQL database running on your computer - you are all set.
If you don't have MySQL installed, you can do it now by installing from: https://dev.mysql.com/doc/mysql-installation-excerpt/5.7/en/
and set it up with user name and password 

Make sure to create a database named **realworld** in your MySQL instance.

#### Run application with IntelliJ

Clone and open ktor-realworld-sample with IntelliJ IDE. Choose **Run / Edit Configuration** menu to create new
launch configuration. On the left side click **[+]** and select **Kotlin**. Name it **RealWorld Server** (or pick other name)
and fill up some essential fields:

- Main class: `io.skinnydoo.RealWorldAppKt`
- Environment variables: `JWT_SECRET=your-secret-here=;DB_USERNAME=root;DB_PWD=your-db-user-password`
- Use classpath or module: `ktor-realworld-sample.main`

You should be able to run/debug your app now.

## License & Credits
Credits have to go out to [Thinkster](https://thinkster.io/) with their awesome [RealWorld](https://github.com/gothinkster/realworld) 

This project is licensed under the MIT license.

## Disclaimer
This source and the whole package comes without warranty. It may or may not harm your computer. Please use with care. Any damage cannot be related back to the author.
