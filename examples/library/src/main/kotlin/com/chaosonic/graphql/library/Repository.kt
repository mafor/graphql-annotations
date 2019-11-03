package com.chaosonic.graphql.library

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.UUIDTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import java.util.*

data class Book (
    val id: String = "",
    val title: String
)

data class Author (
    val id: String = "",
    val name: String
)

data class Page<T>(
    val offset: Int,
    val limit: Int,
    val total: Int,
    val data: Iterable<T>
)

object Books : UUIDTable() {
    val title = varchar("title", 50).index(isUnique = false)
}

object Authors : UUIDTable() {
    val name = varchar("name", 50).index(isUnique = false)
}

object AuthorBook : Table() {
    val author = reference("authorId", Authors).primaryKey(0)
    val book = reference("bookId", Books).primaryKey(1)
}

@Service
class Repository(val database: Database, val observer : Observer) {

    fun listBooks(
        id: String? = null,
        title: String? = null,
        authorId: String? = null,
        offset: Int = 0,
        limit: Int = 10
    ): Page<Book> {

        var where: Op<Boolean> = Op.TRUE

        if (id != null) {
            where = Op.build { Books.id eq UUID.fromString(id) }
        }
        if (title != null) {
            where = where and Op.build { Books.title like title }
        }
        if (authorId != null) {
            where = where and Op.build { AuthorBook.author eq UUID.fromString(authorId) }
        }

        val query = (Books leftJoin AuthorBook)
            .slice(Books.id, Books.title)
            .select { where }
            .groupBy(Books.id, Books.title)

        return transaction(database) {

            val count = query.count()
            val data = query
                .orderBy(Books.title)
                .limit(limit, offset)
                .map(::mapBook)

            Page(offset, limit, count, data)
        }
    }

    private fun mapBook(row: ResultRow) =

        Book(
            id = row[Books.id].value.toString(),
            title = row[Books.title]
        )

    fun listAuthors(
        id: String? = null,
        name: String? = null,
        bookId: String? = null,
        offset: Int = 0,
        limit: Int = 10
    ): Page<Author> {

        var where: Op<Boolean> = Op.TRUE

        if (id != null) {
            where = Op.build { Authors.id eq UUID.fromString(id) }
        }
        if (name != null) {
            where = Op.build { where and (Authors.name like name) }
        }
        if (bookId != null) {
            where = Op.build { where and (AuthorBook.book eq UUID.fromString(bookId)) }
        }

        val query = (Authors leftJoin AuthorBook)
            .slice(Authors.id, Authors.name)
            .select { where }
            .groupBy(Authors.id, Authors.name)

        return transaction(database) {

            val count = query.count()
            val data = query
                .orderBy(Authors.name)
                .limit(limit, offset)
                .map(::mapAuthor)

            Page(offset, limit, count, data)
        }
    }

    private fun mapAuthor(row: ResultRow) =

        Author(
            id = row[Authors.id].value.toString(),
            name = row[Authors.name]
        )

    fun addAuthor(author: Author): Author =

        transaction(database) {

            val authorId = Authors.insertAndGetId {
                it[name] = author.name
            }
            author.copy(authorId.value.toString())

        }.also { observer.onAuthorAdded(it.id) }


    fun addBook(book: Book, authorIds: Iterable<String>): Book =

        transaction(database) {

            val bookId = Books.insertAndGetId {
                it[title] = book.title
            }

            authorIds.forEach { authorId ->
                AuthorBook.insert {
                    it[author] = EntityID(UUID.fromString(authorId),
                        Authors
                    )
                    it[AuthorBook.book] = bookId
                }
            }

            book.copy(bookId.value.toString())

        }.also { observer.onBookAdded(it.id) }


    fun removeBook(id: String): Boolean =

        transaction(database) {
            AuthorBook.deleteWhere { AuthorBook.book eq UUID.fromString(id) }
            Books.deleteWhere { Books.id eq UUID.fromString(id) } > 0
        }.also {
            if(it) observer.onBookRemoved(id)
        }

    fun removeAuthor(id: String): Boolean =

        transaction(database) {
            Authors.deleteWhere { Authors.id eq UUID.fromString(id) } > 0
        }.also {
            if(it) observer.onAuthorRemoved(id)
        }
}
