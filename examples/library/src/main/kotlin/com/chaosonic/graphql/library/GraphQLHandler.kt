package com.chaosonic.graphql.library

import com.chaosonic.graphql.annotations.GraphQLArgument
import com.chaosonic.graphql.annotations.GraphQLHandler
import com.chaosonic.graphql.annotations.GraphQLMapping
import com.chaosonic.graphql.annotations.GraphQLSource

@GraphQLHandler
class GraphQLHandler(val repository: Repository, val observer: Observer) {

    @GraphQLMapping("QueryType", "books")
    fun listBooks(@GraphQLArgument id : String?,
                  @GraphQLArgument authorId : String?,
                  @GraphQLArgument titleRegex : String?): List<Book> =

        repository.listBooksEx(id = id, authorId = authorId, title = titleRegex)


    @GraphQLMapping("Book", "authors")
    fun authors(@GraphQLSource book : Book): List<Author> =

        repository.listAuthors(bookId = book.id)


    @GraphQLMapping("MutationType", "addBook")
    fun addBook(@GraphQLArgument title : String,
                @GraphQLArgument authorIds : List<String>): Book? =

        repository.addBook(Book(title = title), authorIds)


    @GraphQLMapping("MutationType", "removeBook")
    fun removeBook(@GraphQLArgument id : String): Boolean =

        repository.removeBook(id)


    @GraphQLMapping("QueryType", "authors")
    fun listAuthors(@GraphQLArgument id : String?,
                    @GraphQLArgument nameRegex : String?): List<Author> {

        return repository.listAuthors(id = id, name = nameRegex)
    }

    @GraphQLMapping("Author", "books")
    fun books(@GraphQLSource author : Author): List<Book> =

        repository.listBooksEx(authorId = author.id)


    @GraphQLMapping("MutationType", "addAuthor")
    fun addAuthor(@GraphQLArgument name : String): Author? =

        repository.addAuthor(Author(name = name))

    @GraphQLMapping("SubscriptionType", "events")
    fun events() = observer.events
}
