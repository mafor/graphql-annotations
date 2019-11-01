package com.chaosonic.graphql.library.data.impl

import com.chaosonic.graphql.library.data.Author
import com.chaosonic.graphql.library.data.Book
import com.chaosonic.graphql.library.data.DataRepository
import org.jetbrains.exposed.dao.UUIDTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object Books : UUIDTable() {
    val title = varchar("name", 50).index(isUnique = false)
}

object Authors : UUIDTable() {
    val name = varchar("name", 50).index(isUnique = false)
}

object AuthorBook : Table() {
    val author = reference("authorId", Authors).primaryKey(0)
    val book = reference("bookId", Books).primaryKey(1)
}

private fun bookMapper(row: ResultRow) =

    Book(
        row[Books.id].value.toString(),
        row[Books.title],
        emptyList()
    )

private fun authorMapper(row: ResultRow) =

    Author(
        row[Authors.id].value.toString(),
        row[Authors.name]
    )

class DbMemoryDataRepository(val database: Database) : DataRepository {

    override fun listBooks() =

        transaction(database) {
            Books.selectAll().map(::bookMapper)
        }

    override fun getBook(id: String): Book? =

        transaction(database) {

            Books.select { Books.id eq UUID.fromString(id) }
                .map(::bookMapper)
                .firstOrNull()
        }

    override fun listBooks(authorId: String) =

        transaction(database) {

            (AuthorBook innerJoin Books)
                .select { AuthorBook.author eq UUID.fromString(authorId) }
                .map(::bookMapper)
        }

    override fun listAuthors() =

        transaction(database) {
            Authors.selectAll().map(::authorMapper)
        }

    override fun getAuthor(id: String): Author? =

        transaction(database) {
            Authors.select { Authors.id eq UUID.fromString(id) }
                .map(::authorMapper)
                .firstOrNull()
        }

    fun listAuthors(bookId: String) =

        transaction(database) {

            (AuthorBook innerJoin Books)
                .select { AuthorBook.book eq UUID.fromString(bookId) }
                .map(::authorMapper)
        }

    override fun addAuthor(author: Author): Author =

        transaction(database) {

            val authorId = Authors.insertAndGetId {
                it[name] = author.name
            }
            author.copy(authorId.value.toString())
        }


    override fun addBook(book: Book): Book =

        transaction(database) {

            val bookId = Books.insertAndGetId {
                it[title] = book.title
            }

            book.authorIds.map { authorId ->

                val author = Authors.select { Authors.id eq UUID.fromString(authorId) }
                    .firstOrNull()
                    ?.get(Authors.id)

                AuthorBook.insert {
                    it[this.author] = author!!
                    it[this.book] = bookId
                }
            }

            book.copy(bookId.value.toString())
        }


    override fun removeBook(id: String): Boolean = false

}
