package com.chaosonic.graphql.library

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class RepositoryTest {

    @Mock
    lateinit var observer : Observer

    lateinit var repository: Repository

    lateinit var db :Database

    @Before
    fun init() {
        db = Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")
        repository = Repository(db, observer)
    }

    @Test
    fun shouldAddAuthor() {

        transaction {
            //given
            SchemaUtils.create(Books, Authors, AuthorBook)
            // when
            repository.addAuthor(Author(name = "Test"))
            val page = repository.listAuthors()
            // then
            assertThat(page.data).hasSize(1)
            assertThat(page.data.first().name).isEqualTo("Test")
            assertThat(page.data.first().id).isNotNull()
        }
    }

    @Test
    fun shouldFindAuthorByName() {

        transaction {
            //given
            SchemaUtils.create(Books, Authors, AuthorBook)
            val author1 = repository.addAuthor(Author(name = "Test 1"))
            val author2 = repository.addAuthor(Author(name = "Test 2"))
            // when
            val page = repository.listAuthors(name = "% 1")
            // then
            assertThat(page.data).hasSize(1)
            assertThat(page.data.first()).isEqualTo(author1)
        }
    }

    @Test
    fun shouldFindAuthorById() {

        transaction {
            //given
            SchemaUtils.create(Books, Authors, AuthorBook)
            val author1 = repository.addAuthor(Author(name = "Test 1"))
            val author2 = repository.addAuthor(Author(name = "Test 2"))
            // when
            val page = repository.listAuthors(id = author1.id)
            // then
            assertThat(page.data).hasSize(1)
            assertThat(page.data.first()).isEqualTo(author1)
        }
    }

    @Test
    fun shouldRemoveAuthor() {

        transaction {
            //given
            SchemaUtils.create(Books, Authors, AuthorBook)
            val author1 = repository.addAuthor(Author(name = "Test 1"))
            val author2 = repository.addAuthor(Author(name = "Test 2"))
            // when
            repository.removeAuthor(id = author1.id)
            val page = repository.listAuthors()
            // then
            assertThat(page.data).hasSize(1)
            assertThat(page.data.first()).isEqualTo(author2)
        }
    }

    @Test
    fun shouldAddBook() {

        transaction {
            //given
            SchemaUtils.create(Books, Authors, AuthorBook)
            val author = repository.addAuthor(Author(name = "Test"))
            // when
            val book = repository.addBook(Book(title = "Test"), listOf(author.id))
            val page = repository.listAuthors(bookId = book.id)
            var authors =
            // then
            assertThat(book.id).isNotBlank()
            assertThat(book.title).isEqualTo("Test")
            assertThat(page.data).hasSize(1)
            assertThat(page.data.first()).isEqualTo(author)
        }
    }

    @Test
    fun shouldFindBookByAuthor() {

        transaction {
            //given
            SchemaUtils.create(Books, Authors, AuthorBook)
            val author1 = repository.addAuthor(Author(name = "Author 1"))
            val author2 = repository.addAuthor(Author(name = "Author 2"))
            val book1 = repository.addBook(Book(title = "Book 1"), listOf(author1.id))
            val book2 = repository.addBook(Book(title = "Book 2"), listOf(author2.id))
            // when
            val page = repository.listBooks(authorId = author1.id)
             // then
            assertThat(page.data).hasSize(1)
            assertThat(page.data.first()).isEqualTo(book1)
        }
    }

    @Test
    fun shouldFindBookByTitle() {

        transaction {
            //given
            SchemaUtils.create(Books, Authors, AuthorBook)
            val author1 = repository.addAuthor(Author(name = "Author 1"))
            val author2 = repository.addAuthor(Author(name = "Author 2"))
            val book1 = repository.addBook(Book(title = "Book 1"), listOf(author1.id))
            val book2 = repository.addBook(Book(title = "Book 2"), listOf(author2.id))
            // when
            val page = repository.listBooks(title = "% 2")
            // then
            assertThat(page.data).hasSize(1)
            assertThat(page.data.first()).isEqualTo(book2)
        }
    }

    @Test
    fun shouldFindBookById() {

        transaction {
            //given
            SchemaUtils.create(Books, Authors, AuthorBook)
            val author1 = repository.addAuthor(Author(name = "Author 1"))
            val author2 = repository.addAuthor(Author(name = "Author 2"))
            val book1 = repository.addBook(Book(title = "Book 1"), listOf(author1.id))
            val book2 = repository.addBook(Book(title = "Book 2"), listOf(author2.id))
            // when
            val page = repository.listBooks(id = book1.id)
            // then
            assertThat(page.data).hasSize(1)
            assertThat(page.data.first()).isEqualTo(book1)
        }
    }

    fun shouldRemoveBook() {

        transaction {
            //given
            SchemaUtils.create(Books, Authors, AuthorBook)
            val author1 = repository.addAuthor(Author(name = "Author 1"))
            val book1 = repository.addBook(Book(title = "Book 1"), listOf(author1.id))
            val book2 = repository.addBook(Book(title = "Book 2"), listOf(author1.id))
            // when
            repository.removeBook(book1.id)
            val page = repository.listBooks(id = book1.id)
            // then
            assertThat(page.data).hasSize(1)
            assertThat(page.data.first()).isEqualTo(book2)
        }
    }

    fun shouldSupportPagination() {

        transaction {
            //given
            SchemaUtils.create(Books, Authors, AuthorBook)
            val author1 = repository.addAuthor(Author(name = "Author 1"))
            val book1 = repository.addBook(Book(title = "Book 1"), listOf(author1.id))
            val book2 = repository.addBook(Book(title = "Book 2"), listOf(author1.id))
            // when
            var page = repository.listBooks(offset = 0, limit = 1)
            // then
            assertThat(page.total).isEqualTo(2)
            assertThat(page.limit).isEqualTo(1)
            assertThat(page.offset).isEqualTo(0)
            assertThat(page.data).hasSize(1)
            assertThat(page.data.first()).isEqualTo(book1)
            // when
            page = repository.listBooks(offset = 1, limit = 1)
            // then
            assertThat(page.total).isEqualTo(2)
            assertThat(page.limit).isEqualTo(1)
            assertThat(page.offset).isEqualTo(0)
            assertThat(page.data).hasSize(1)
            assertThat(page.data.first()).isEqualTo(book2)
        }
    }
}