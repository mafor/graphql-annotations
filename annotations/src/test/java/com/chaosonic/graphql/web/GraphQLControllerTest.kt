package com.chaosonic.graphql.web

import com.chaosonic.graphql.annotations.any
import graphql.ExecutionInput
import graphql.ExecutionResult
import graphql.GraphQL
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.test.context.junit4.SpringRunner
import reactor.core.publisher.Flux
import java.net.URI
import java.util.Collections.singletonMap
import java.util.concurrent.CompletableFuture.completedFuture

@RunWith(SpringRunner::class)
@EnableAutoConfiguration
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [GraphQLController::class])
class GraphQLControllerTest {

    @Autowired
    lateinit private var testRestTemplate: TestRestTemplate

    @Value("\${graphql.path:graphql}")
    lateinit private var path: String

    @MockBean
    lateinit private var graphQL: GraphQL

    @Mock
    lateinit private var executionResult: ExecutionResult

    lateinit private var request: ArgumentCaptor<ExecutionInput>

    @Before
    fun init() {
        `when`(graphQL.executeAsync(any<ExecutionInput>())).thenReturn(completedFuture(executionResult))
        `when`(executionResult.toSpecification()).thenReturn(singletonMap("authors", emptyList<Any>() as Any))
        request = ArgumentCaptor.forClass(ExecutionInput::class.java)
    }

    @Test
    fun requestGet() {
        // given
        val query = "{authors {name}}"
        // when
        val resp = getRequest(query)
        // then
        verify(graphQL).executeAsync(request.capture())
        // and
        assertThat(request.value.query).isEqualTo(query)
        assertThat(request.value.variables).isNullOrEmpty()
        assertThat(request.value.operationName).isNullOrEmpty()
        // and
        assertThat(resp.statusCodeValue).isEqualTo(200)
        assertThat(resp.headers.contentType).isEqualTo(MediaType.APPLICATION_JSON)
        assertThat(resp.body).isEqualTo("""{"authors":[]}""")
    }

    @Test
    fun requestPost() {
        // given
        val body = """{"query": "{authors {name}}"}"""
        // when
        val resp = postRequest(body)
        // then
        verify(graphQL).executeAsync(request.capture())
        //a nd
        assertThat(request.value.query).isEqualTo("{authors {name}}")
        assertThat(request.value.variables).isNullOrEmpty()
        assertThat(request.value.operationName).isNullOrEmpty()
        // and
        assertThat(resp.statusCodeValue).isEqualTo(200)
        assertThat(resp.headers.contentType).isEqualTo(MediaType.APPLICATION_JSON)
        assertThat(resp.body).isEqualTo("""{"authors":[]}""")
    }

    @Test
    fun requestPostWithVariables() {
        // given
        val body = """
            {"query": "{authors {name}}",
             "variables": {
                "param": "value"
              }
            }
            """.trimIndent()
        // when
        val resp = postRequest(body)
        // then
        verify(graphQL).executeAsync(request.capture())
        // and
        assertThat(request.value.query).isEqualTo("{authors {name}}")
        assertThat(request.value.variables).isEqualTo(singletonMap("param","value"))
        assertThat(request.value.operationName).isNullOrEmpty()
        // and
        assertThat(resp.statusCodeValue).isEqualTo(200)
        assertThat(resp.headers.contentType).isEqualTo(MediaType.APPLICATION_JSON)
        assertThat(resp.body).isEqualTo("""{"authors":[]}""")
    }

    @Test
    fun requestPostForEventStream() {
        // given
        val body = """{"query": "{authors {name}}"}"""
        `when`(executionResult.getData() as Any?).thenReturn(Flux.just(mock(ExecutionResult::class.java)))
        // when
        val resp = postRequest(body, accept = MediaType.TEXT_EVENT_STREAM)
        // then
        verify(graphQL).executeAsync(request.capture())
        // and
        assertThat(request.value.query).isEqualTo("{authors {name}}")
        assertThat(request.value.variables).isNullOrEmpty()
        assertThat(request.value.operationName).isNullOrEmpty()
        // and
        assertThat(resp.headers.contentType?.isCompatibleWith(MediaType.TEXT_EVENT_STREAM)).isTrue()
    }

    fun getRequest(query :String) =

       testRestTemplate.getForEntity("/{path}?query={query}", String::class.java, path, query)

    fun postRequest(body :String, accept: MediaType = MediaType.APPLICATION_JSON) =

        RequestEntity.post(URI.create("/$path"))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(accept)
            .body(body)
            .let { testRestTemplate.exchange(it, String::class.java) }

}