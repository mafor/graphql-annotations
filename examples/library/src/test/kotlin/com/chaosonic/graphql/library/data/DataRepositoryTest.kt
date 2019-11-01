package com.chaosonic.graphql.library.data

import com.chaosonic.graphql.library.AuthorBook
import com.chaosonic.graphql.library.Authors
import com.chaosonic.graphql.library.Books
import com.chaosonic.graphql.library.DbMemoryDataRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Test

class DataRepositoryTest {


    /*class Author(id: EntityID<Long>) : LongEntity(id) {
        companion object : LongEntityClass<Author>(Authors)

        var name by Authors.name
        var books by Book via AuthorBook
    }

    class Book(id: EntityID<Long>) : LongEntity(id) {
        companion object : LongEntityClass<Book>(Books)

        var name by Books.name
        var authors by Author via AuthorBook
    }

    class AuthorPOJO(val id: Long, val name: String)

    class BookPOJO(val id: Long, val name: String)

    @Test
    fun daoTest() {

        Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")

        transaction {

            SchemaUtils.create(Books, Authors, AuthorBook)

            var author = transaction {
                Author.new { name = "Johny" }
            }

            val book = transaction {
                Book.new { name = "Test" }
            }

            transaction {
                book.authors = SizedCollection(author)
            }

            author = Author.get(author.id)

            assertThat(author.id.value).isEqualTo(1)
            assertThat(author.name).isEqualTo("Johny")
            assertThat(author.books).hasSize(1)
            assertThat(author.books.first().id.value).isEqualTo(1)
            assertThat(author.books.first().name).isEqualTo("Test")
            assertThat(author.books.first().authors).hasSize(1)
        }
    }

    @Test
    fun dslTest() {

        Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")

        transaction {

            SchemaUtils.create(Books, Authors, AuthorBook)

            val authorId = Authors.insertAndGetId {
                it[name] = "Johny"
            }
            val bookId = Books.insertAndGetId {
                it[name] = "Test"
            }
            AuthorBook.insert {
                it[author] = authorId
                it[book] = bookId
            }

            val authors = Authors.selectAll().map {
                AuthorPOJO(
                    it[Authors.id].value,
                    it[Authors.name])
            }

            val books = Books.selectAll().map {
                BookPOJO(
                    it[Books.id].value,
                    it[Books.name])
            }
        }

    }*/

    @Test
    fun daoTest() {

        val db = Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")
        val repo = DbMemoryDataRepository(db)

        transaction(db) {
            SchemaUtils.create(
                Books,
                Authors,
                AuthorBook
            )
            val author = repo.addAuthor(Author(name = "Johny"))
            repo.addBook(Book(title = "Test", authorIds = listOf(author.id)))
        }

    }
}