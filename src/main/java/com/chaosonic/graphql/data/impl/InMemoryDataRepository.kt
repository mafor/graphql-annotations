package com.chaosonic.graphql.data.impl

import com.chaosonic.graphql.data.Author
import com.chaosonic.graphql.data.Book
import com.chaosonic.graphql.data.DataRepository
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
private data class Data @JsonCreator constructor(
    @JsonProperty("books") val books: Array<Book>,
    @JsonProperty("authors") val authors: Array<Author>
)

private abstract class BookMixin @JsonCreator constructor(
    @JsonProperty("id") val id: String = "",
    @JsonProperty("title") val title: String,
    @JsonProperty("authorIds") val authorIds:  List<String>
)

private abstract class AuthorMixin @JsonCreator constructor(
    @JsonProperty("id") val id: String = "",
    @JsonProperty("name") val name: String
)

@Configuration
class DataRepositoryConfiguration {

    @Bean
    fun dataRepository(@Value("classpath:/data/data.yml") dataYml: Resource): DataRepository =

        dataYml.inputStream.use {

            val data = ObjectMapper(YAMLFactory())
                .addMixIn(Author::class.java, AuthorMixin::class.java)
                .addMixIn(Book::class.java, BookMixin::class.java)
                .readValue(dataYml.inputStream, Data::class.java)

            InMemoryDataRepository(data)
        }

}

private class InMemoryDataRepository(var data: Data) : DataRepository {

    override fun listBooks() = data.books.asList()

    override fun getBook(id: String): Book? = data.books.firstOrNull { it.id == id }

    override fun listBooks(authorId: String) = data.books.filter { it.authorIds.contains(authorId) }

    override fun listAuthors() = data.authors.asList()

    override fun getAuthor(id: String): Author? = data.authors.firstOrNull { it.id == id }

    override fun addAuthor(author: Author): Author {

        val author = author.copy(id = UUID.randomUUID().toString())
        data = data.copy(authors = arrayOf(author, *data.authors))

        return author
    }

    override fun addBook(book: Book): Book {

        book.authorIds.forEach { if (getAuthor(it) == null) throw RuntimeException("Author with the id $it does not exist") }

        val book = book.copy(id = UUID.randomUUID().toString())
        data = data.copy(books = arrayOf(book, *data.books))

        return book
    }

    override fun removeBook(id: String): Boolean {

        val books = data.books.filter { it.id != id }.toTypedArray()
        val result = books.size < data.books.size
        data = data.copy(books = books)

        return result
    }

}
