package com.chaosonic.graphql.library

import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Primary
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

    @MockBean
    private lateinit var repository: Repository

    private val jsonPathConfig = Configuration.defaultConfiguration().setOptions(Option.SUPPRESS_EXCEPTIONS)

    @Test
    fun shouldCallListAuthors() {
        // given
        `when`(repository.listAuthors())
            .thenReturn(Page(0, 10, 1, listOf(Author("id", "name"))))
        `when`(repository.listBooks(authorId = "id"))
            .thenReturn(Page(0, 10, 1, listOf(Book("id", "title"))))
        // when
        postRequest ("{authors {data {name, books{title}}}}")
        // then
        verify (repository).listAuthors()
        verify (repository).listBooks(authorId = "id")
    }

    @Test
    fun shouldCallListAuthorsWithParameters() {
        // when
        postRequest("{authors(name: \"test\", offset: 5, limit: 5) {data {name}}}")
        // then
        verify(repository).listAuthors(name = "test", offset = 5, limit = 5)
    }

    @Test
    fun shouldCallAddAuthor() {
        // when
        postRequest("mutation {addAuthor(name: \"test\") {id, name}}")
        // then
        verify(repository).addAuthor(Author(name = "test"))
    }

    @Test
    fun shouldCallListBooks() {
        // given
        `when`(repository.listBooks())
            .thenReturn(Page(0, 10, 1, listOf(Book("id", "title"))))
        `when`(repository.listAuthors(bookId = "id"))
            .thenReturn(Page(0, 10, 1, listOf(Author("id", "name"))))
        // when
        postRequest("{books {data {title, authors{name}}}}")
        // then
        verify(repository).listBooks()
        verify(repository).listAuthors(bookId = "id")
    }

    @Test
    fun shouldCallListBooksWithParameters() {
        // when
        postRequest("{books(title: \"test\", authorId:\"x\", offset: 5, limit: 5) {data {title}}}")
        // then
        verify(repository).listBooks(title = "test", authorId = "x", offset = 5, limit = 5)
    }

    @Test
    fun shouldCallAddBook() {
        // when
        postRequest("mutation {addBook(title: \"test\", authorIds: [\"test\"]) {id, title}}")
        // then
        verify(repository).addBook(Book(title = "test"), listOf("test"))
    }

    @Test
    fun shouldCallRemoveBook() {
        // when
        postRequest("mutation {removeBook(id: \"test\")}")
        // then
        verify(repository).removeBook("test")
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