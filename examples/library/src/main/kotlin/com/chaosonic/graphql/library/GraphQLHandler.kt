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
                  @GraphQLArgument title : String?,
                  @GraphQLArgument offset: Int?,
                  @GraphQLArgument limit: Int?): Page<Book> =

        repository.listBooks(
            id = id,
            authorId = authorId,
            title = title,
            offset = offset ?: 0,
            limit = limit ?: 10)


    @GraphQLMapping("Book", "authors")
    fun authors(@GraphQLSource book : Book): Iterable<Author> =

        repository.listAuthors(bookId = book.id).data


    @GraphQLMapping("MutationType", "addBook")
    fun addBook(@GraphQLArgument title : String,
                @GraphQLArgument authorIds : List<String>): Book? =

        repository.addBook(Book(title = title), authorIds)


    @GraphQLMapping("MutationType", "removeBook")
    fun removeBook(@GraphQLArgument id : String): Boolean =

        repository.removeBook(id)


    @GraphQLMapping("QueryType", "authors")
    fun listAuthors(@GraphQLArgument id : String?,
                    @GraphQLArgument name : String?,
                    @GraphQLArgument offset: Int?,
                    @GraphQLArgument limit: Int?): Page<Author> {

        return repository.listAuthors(
            id = id,
            name = name,
            offset = offset ?: 0,
            limit = limit ?: 10)
    }

    @GraphQLMapping("Author", "books")
    fun books(@GraphQLSource author : Author,
              @GraphQLArgument titleRegex : String?,
              @GraphQLArgument offset: Int?,
              @GraphQLArgument limit: Int?): Iterable<Book> =

        repository.listBooks(
            authorId = author.id,
            title = titleRegex,
            offset = offset ?: 0,
            limit = limit ?: 10).data


    @GraphQLMapping("MutationType", "addAuthor")
    fun addAuthor(@GraphQLArgument name : String): Author? =

        repository.addAuthor(Author(name = name))

    @GraphQLMapping("SubscriptionType", "events")
    fun events() = observer.events
}
