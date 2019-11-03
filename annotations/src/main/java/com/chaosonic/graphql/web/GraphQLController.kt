package com.chaosonic.graphql.web

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import graphql.ExecutionInput
import graphql.ExecutionResult
import graphql.GraphQL
import org.reactivestreams.Publisher
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@RestController
@RequestMapping(path = ["\${graphql.path:graphql}"])
class GraphQLController(val graphQl: GraphQL) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    class Request {

        @NotBlank
        var query: String? = null
        var operationName: String? = null
        val variables: Map<String, Any> = HashMap()

        fun toExecutionInput() =
            ExecutionInput.newExecutionInput()
                .variables(variables)
                .operationName(operationName)
                .query(query)
                .build()
    }

    @RequestMapping(
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun requestGet(@Valid request : Request) = getSingleResult(request.toExecutionInput())


    @RequestMapping(
        method = [RequestMethod.GET],
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @ResponseBody
    fun requestGetStream(@Valid request : Request) = getStream(request.toExecutionInput())


    @RequestMapping(
        method = [RequestMethod.POST],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun requestPost(@RequestBody request: Mono<Request>) =

        request.map { it.toExecutionInput() }.flatMap { getSingleResult(it) }


    @RequestMapping(
        method = [RequestMethod.POST],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @ResponseBody
    fun requestPostStream(@RequestBody request: Mono<Request>) =

        request.map { it.toExecutionInput() }.flatMapMany { getStream(it) }


    private fun getSingleResult(request: ExecutionInput) =

        Mono.fromCompletionStage(graphQl.executeAsync(request)).map { it.toSpecification() }

    private fun getStream(request: ExecutionInput) =

        Mono.fromCompletionStage(graphQl.executeAsync(request))
            .flatMapMany { unfold(it) }
            .map { it.toSpecification() }

    private fun unfold(executionResult: ExecutionResult) =

        if (executionResult.getData() as Any? is Publisher<*>) {
            Flux.from(executionResult.getData())
        } else {
            Flux.just(executionResult)
        }
}
