package com.chaosonic.graphql.library

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class GraphQLApplication {

    @Bean
    fun database() : Database {

        val db = Database.connect("jdbc:h2:~/test", driver = "org.h2.Driver")
        transaction(db) { SchemaUtils.create(
            Books,
            Authors,
            AuthorBook
        ) }

        return db
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(GraphQLApplication::class.java, *args)
}
