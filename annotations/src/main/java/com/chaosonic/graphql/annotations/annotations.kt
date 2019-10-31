package com.chaosonic.graphql.annotations

import org.springframework.stereotype.Component

/**
 * Marks classes which should be searched for methods
 * annotated with the [GraphQLMapping] annotation
 */
@Component
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class GraphQLHandler

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class GraphQLMapping(
    val type: String,
    val field: String
)

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class GraphQLArgument(
    val name: String
)

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class GraphQLSource