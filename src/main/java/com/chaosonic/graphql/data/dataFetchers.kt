package com.chaosonic.graphql.data

import com.chaosonic.graphql.lib.DataFetcher
import graphql.schema.DataFetchingEnvironment

@DataFetcher("QueryType", "books")
class BooksQuery(val dataRepository: DataRepository) : graphql.schema.DataFetcher<List<Book>> {

    override fun get(environment: DataFetchingEnvironment): List<Book> {

        var books = dataRepository.listBooks().asSequence()

        if (environment.containsArgument("title")) {
            books = books.filter { b -> b.title == environment.getArgument("title") }
        }
        if (environment.containsArgument("id")) {
            books = books.filter { b -> b.id == environment.getArgument("id") }
        }

        return books.toList()
    }

}

@DataFetcher("Book", "authors")
class BookAuthorFetcher(val dataRepository: DataRepository) : graphql.schema.DataFetcher<List<Author>> {

    override fun get(environment: DataFetchingEnvironment): List<Author> =
        environment.getSource<Book>().authorIds.mapNotNull { dataRepository.getAuthor(it) }

}

@DataFetcher("MutationType", "addBook")
class AddBookMutation(val dataRepository: DataRepository) : graphql.schema.DataFetcher<Book> {

    override fun get(environment: DataFetchingEnvironment): Book? =
        dataRepository.addBook(
            Book(title = environment.getArgument("title"),
                authorIds = environment.getArgument("authorIds")))

}

@DataFetcher("MutationType", "removeBook")
class RemoveBookMutation(val dataRepository: DataRepository) : graphql.schema.DataFetcher<Boolean> {

    override fun get(environment: DataFetchingEnvironment): Boolean =
        dataRepository.removeBook(environment.getArgument("id"))

}

@DataFetcher("QueryType", "authors")
class AuthorsQuery(val dataRepository: DataRepository) : graphql.schema.DataFetcher<List<Author>> {

    override fun get(environment: DataFetchingEnvironment): List<Author> {

        var authors = dataRepository.listAuthors().asSequence()

        if (environment.containsArgument("id")) {
            authors = authors.filter { b -> b.id == environment.getArgument("id") }
        }

        return authors.toList()
    }

}

@DataFetcher("Author", "books")
class AuthorBooksFetcher(val dataRepository: DataRepository) : graphql.schema.DataFetcher<List<Book>> {

    override fun get(environment: DataFetchingEnvironment): List<Book> =
        dataRepository.listBooks(environment.getSource<Author>().id)

}

@DataFetcher("MutationType", "addAuthor")
class AddAuthorMutation(val dataRepository: DataRepository) : graphql.schema.DataFetcher<Author> {

    override fun get(environment: DataFetchingEnvironment): Author? =
        dataRepository.addAuthor(
            Author(name = environment.getArgument("name")))

}
