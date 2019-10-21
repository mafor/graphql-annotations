package com.chaosonic.graphql.lib

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import graphql.*
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@JsonIgnoreProperties(ignoreUnknown = true)
class Request {
    var query : String? = null
    var operationName : String? = null
    val variables : Map<String, String> = HashMap()
}

@RestController
@RequestMapping(path = ["\${graphql.path:graphql}"])
class RequestController(val graphQl: GraphQL) {

    @RequestMapping(
        method = [RequestMethod.GET],
        produces = ["application/json"])
    @ResponseBody
    fun requestGet(@RequestParam(name = "query") query: String) =

        Mono.just(ExecutionInput.newExecutionInput().query(query))
            .flatMap { Mono.fromCompletionStage(graphQl.executeAsync(it)) }
            .map { it.toSpecification() }

    @RequestMapping(
        method = [RequestMethod.POST],
        consumes = [
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_JSON_UTF8_VALUE],
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
            .onErrorReturn(ExecutionResultImpl.newExecutionResult().addError(null).build())
            .map { it.toSpecification() }

}
