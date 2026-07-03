package com.ebookstore.repository

import com.ebookstore.EbookStoreApplication
import com.ebookstore.entity.Book
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@DataJpaTest
@ContextConfiguration(classes = EbookStoreApplication)
class BookRepositorySpec extends Specification {

    @Autowired
    BookRepository bookRepository

    private Book makeBook(String title, String author, String genre, double price, boolean available = true) {
        Book b = new Book()
        b.title = title
        b.author = author
        b.description = "A great book"
        b.price = new BigDecimal(price.toString())
        b.genre = genre
        b.available = available
        return b
    }

    def "save and find book by id"() {
        given:
        Book book = bookRepository.save(makeBook("Clean Code", "Robert Martin", "Technology", 29.99))

        when:
        def found = bookRepository.findById(book.id)

        then:
        found.isPresent()
        found.get().title == "Clean Code"
        found.get().author == "Robert Martin"
    }

    def "findByAvailableTrue returns only available books"() {
        given:
        bookRepository.save(makeBook("Available Book", "Author A", "Fiction", 9.99, true))
        bookRepository.save(makeBook("Unavailable Book", "Author B", "Fiction", 9.99, false))

        when:
        def result = bookRepository.findByAvailableTrue()

        then:
        result.size() == 1
        result[0].title == "Available Book"

        cleanup:
        bookRepository.deleteAll()
    }

    def "findByTitleContainingIgnoreCase performs case-insensitive partial match"() {
        given:
        bookRepository.save(makeBook("Spring Boot in Action", "Craig Walls", "Technology", 39.99))
        bookRepository.save(makeBook("Effective Java", "Joshua Bloch", "Technology", 45.00))

        when:
        def result = bookRepository.findByTitleContainingIgnoreCase("spring")

        then:
        result.size() == 1
        result[0].title == "Spring Boot in Action"

        cleanup:
        bookRepository.deleteAll()
    }

    def "findByAuthorContainingIgnoreCase performs case-insensitive partial match"() {
        given:
        bookRepository.save(makeBook("Book One", "Martin Fowler", "Technology", 20.00))
        bookRepository.save(makeBook("Book Two", "Kent Beck", "Technology", 20.00))

        when:
        def result = bookRepository.findByAuthorContainingIgnoreCase("martin")

        then:
        result.size() == 1
        result[0].author == "Martin Fowler"

        cleanup:
        bookRepository.deleteAll()
    }

    def "findByGenreIgnoreCase is case insensitive"() {
        given:
        bookRepository.save(makeBook("Fantasy Book", "Author X", "Fantasy", 15.00))

        when:
        def result = bookRepository.findByGenreIgnoreCase("FANTASY")

        then:
        result.size() == 1
        result[0].genre == "Fantasy"

        cleanup:
        bookRepository.deleteAll()
    }

    def "deleteById removes the book"() {
        given:
        Book book = bookRepository.save(makeBook("Temp Book", "Temp Author", "Misc", 5.00))

        when:
        bookRepository.deleteById(book.id)

        then:
        !bookRepository.existsById(book.id)
    }
}
