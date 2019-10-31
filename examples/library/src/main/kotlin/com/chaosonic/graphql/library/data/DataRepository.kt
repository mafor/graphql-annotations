package com.chaosonic.graphql.library.data

data class Book (
    val id: String = "",
    val title: String,
    val authorIds:  List<String>
)

data class Author (
    val id: String = "",
    val name: String
)

interface DataRepository {

    fun listBooks(): List<Book>
    fun listBooks(authorId : String): List<Book>
    fun getBook(id : String): Book?
    fun addBook(book: Book): Book
    fun removeBook(id: String): Boolean

    fun listAuthors(): List<Author>
    fun getAuthor(id : String): Author?
    fun addAuthor(author : Author): Author?
}

