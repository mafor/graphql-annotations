package com.chaosonic.graphql

import com.chaosonic.graphql.data.Author
import com.chaosonic.graphql.data.Book
import com.chaosonic.graphql.data.DataRepository
import com.chaosonic.graphql.lib.GraphQLArgument
import com.chaosonic.graphql.lib.GraphQLController
import com.chaosonic.graphql.lib.GraphQLMapping
import com.chaosonic.graphql.lib.GraphQLSource

@GraphQLController
class GraphQLController(val dataRepository: DataRepository) {

    @GraphQLMapping("QueryType", "books")
    fun listBooks(@GraphQLArgument("id") id : String?,
                  @GraphQLArgument("authorId") authorId : String?,
                  @GraphQLArgument("titleRegex") titleRegex : String?): List<Book> {

        if (id != null) {
            val book = dataRepository.getBook(id)
            return if(book != null) listOf(book) else emptyList()
        }

        var books = dataRepository.listBooks().asSequence()

        if (authorId != null) {
            books = books.filter { b -> b.authorIds.contains(authorId) }
        }
        if (titleRegex != null) {
            val regex = titleRegex.toRegex()
            books = books.filter { b -> regex.matches(b.title) }
        }

        return books.toList()
    }

    @GraphQLMapping("Book", "authors")
    fun authors(@GraphQLSource book : Book): List<Author> =

        book.authorIds.mapNotNull { dataRepository.getAuthor(it) }


    @GraphQLMapping("MutationType", "addBook")
    fun addBook(
        @GraphQLArgument("title") title : String,
        @GraphQLArgument("authorIds") authorIds : List<String>): Book? =

        dataRepository.addBook(Book(title = title, authorIds = authorIds))


    @GraphQLMapping("MutationType", "removeBook")
    fun removeBook(@GraphQLArgument("id") id : String): Boolean =

        dataRepository.removeBook(id)


    @GraphQLMapping("QueryType", "authors")
    fun listAuthors(@GraphQLArgument("id") id : String?,
                    @GraphQLArgument("nameRegex") nameRegex : String?): List<Author> {

        if (id != null) {
            val author = dataRepository.getAuthor(id)
            return if(author != null) listOf(author) else emptyList()
        }

        var authors = dataRepository.listAuthors().asSequence()

        if (nameRegex != null) {
            val regex = nameRegex.toRegex()
            authors = authors.filter { b -> regex.matches(b.name) }
        }
        return authors.toList()
    }

    @GraphQLMapping("Author", "books")
    fun books(@GraphQLSource author : Author): List<Book> =

        dataRepository.listBooks(author.id)


    @GraphQLMapping("MutationType", "addAuthor")
    fun addAuthor(@GraphQLArgument("name") name : String): Author? =

        dataRepository.addAuthor(Author(name = name))
}
