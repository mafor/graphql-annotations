package com.chaosonic.graphql.library

import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.test.context.junit4.SpringRunner
import java.net.URI

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [GraphQLApplication::class])
class GraphQlApplicationTests {

    @Autowired
    private var testRestTemplate: TestRestTemplate? = null

    @Value("\${graphql.path:graphql}")
    private var path: String? = null

    private val jsonPathConfig = Configuration.defaultConfiguration().setOptions(Option.SUPPRESS_EXCEPTIONS)

    @Test
    fun shouldReturnAllAuthors() {

        val document = getRequest("{authors {name}}")

        assertThat(document.read("$.errors", List::class.java)).isNull()
        assertThat(document.read("$.data", Object::class.java)).isNotNull()
        assertThat(document.read("$.data.authors.length()", Int::class.java)).isEqualTo(2)
    }

    @Test
    fun shouldReturnAllBooks() {

        val document = getRequest("{books {title}}")

        assertThat(document.read("$.errors", List::class.java)).isNull()
        assertThat(document.read("$.data", Object::class.java)).isNotNull()
        assertThat(document.read("$.data.books.length()", Int::class.java)).isEqualTo(3)
    }

    @Test
    fun shouldReturnOneBook() {

        val document = getRequest("{books(id: \"1\") {id, title, authors {name}}}")

        assertThat(document.read<List<*>>("$.errors")).isNull()
        assertThat(document.read("$.data", Object::class.java)).isNotNull()
        assertThat(document.read("$.data.books.length()", Int::class.java)).isEqualTo(1)
        assertThat(document.read("$.data.books[0].id", Int::class.java)).isEqualTo(1)
        assertThat(document.read("$.data.books[0].title", String::class.java)).isEqualTo("Book 1")
        assertThat(document.read("$.data.books[0].authors.length()", Int::class.java)).isGreaterThan(0)

    }

    @Test
    fun shouldReturnOneAuthor() {

        val document = postRequest("{authors(id: \"1\") {id, name, books {title}}}")

        assertThat(document.read("$.errors", List::class.java)).isNull()
        assertThat(document.read("$.data", Object::class.java)).isNotNull()
        assertThat(document.read("$.data.authors.length()", Int::class.java)).isEqualTo(1)
        assertThat(document.read("$.data.authors[0].id", Int::class.java)).isEqualTo(1)
        assertThat(document.read("$.data.authors[0].name", String::class.java)).isEqualTo("Author 1")
        assertThat(document.read("$.data.authors[0].books.length()", Int::class.java)).isGreaterThan(0)

    }

    fun getRequest(query :String) : DocumentContext {

        val response = testRestTemplate!!.getForEntity("/{path}?query={query}", String::class.java, path, query)
        assertThat(response.statusCodeValue).isEqualTo(200)

        return JsonPath.using(jsonPathConfig).parse(response.body)
    }

    fun postRequest(query :String) : DocumentContext {

        val request = RequestEntity.post(URI.create("/$path"))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"query\": \"${query.replace("\"", "\\\"")}\"}")

        val response = testRestTemplate!!.exchange(request, String::class.java)
        assertThat(response.statusCodeValue).isEqualTo(200)

        return JsonPath.using(jsonPathConfig).parse(response.body)
    }

}