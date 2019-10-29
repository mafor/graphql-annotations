package com.chaosonic.graphql.web

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import graphql.ExecutionInput
import graphql.GraphQL
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@ConditionalOnBean(GraphQL::class)
@RestController
@RequestMapping(path = ["\${graphql.path:graphql}"])
class GraphQLController(val graphQl: GraphQL) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    class Request {
        var query: String? = null
        var operationName: String? = null
        val variables: Map<String, String> = HashMap()
    }

    @RequestMapping(
        method = [RequestMethod.GET],
        produces = ["application/json"])
    @ResponseBody
    fun requestGet(
        @RequestParam(name = "query") query: String,
        @RequestParam(name = "variables", required = false) variables: Map<String, Any>?,
        @RequestParam(name = "operationName", required = false) operationName: String?) =

        Mono.just(
                ExecutionInput.newExecutionInput()
                    .variables(variables)
                    .operationName(operationName)
                    .query(query)
            )
            .flatMap { Mono.fromCompletionStage(graphQl.executeAsync(it)) }
            .map { it.toSpecification() }

    @RequestMapping(
        method = [RequestMethod.POST],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = ["application/json"])
    @ResponseBody
    fun requestPost(@RequestBody request: Mono<Request>) =

        request.map {
                ExecutionInput.newExecutionInput()
                    .variables(it.variables)
                    .operationName(it.operationName)
                    .query(it.query)
            }
            .flatMap { Mono.fromCompletionStage(graphQl.executeAsync(it)) }
            .map { it.toSpecification() }

}