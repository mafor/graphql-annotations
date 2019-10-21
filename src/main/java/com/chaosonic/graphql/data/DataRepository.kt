package com.chaosonic.graphql.data

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class Book @JsonCreator constructor(
    @JsonProperty("id") val id: String = "",
    @JsonProperty("title") val title: String,
    @JsonProperty("authorIds") val authorIds:  List<String>
)

data class Author @JsonCreator constructor(
    @JsonProperty("id") val id: String = "",
    @JsonProperty("name") val name: String
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

