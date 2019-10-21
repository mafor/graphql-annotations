package com.chaosonic.graphql

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class ConfigClientApplication

fun main(args: Array<String>) {
    SpringApplication.run(ConfigClientApplication::class.java, *args)
}
