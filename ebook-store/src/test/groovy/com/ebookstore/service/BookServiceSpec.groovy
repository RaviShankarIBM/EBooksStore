package com.ebookstore.service

import com.ebookstore.dto.BookRequest
import com.ebookstore.entity.Book
import com.ebookstore.exception.ResourceNotFoundException
import com.ebookstore.repository.BookRepository
import spock.lang.Specification
import spock.lang.Subject

import java.math.BigDecimal

class BookServiceSpec extends Specification {

    BookRepository bookRepository = Mock()

    @Subject
    BookService bookService = new BookService(bookRepository)

    private Book sampleBook(Long id = 1L) {
        Book b = new Book()
        b.id = id
        b.title = "Clean Code"
        b.author = "Robert Martin"
        b.description = "A handbook of agile software craftsmanship"
        b.price = new BigDecimal("29.99")
        b.genre = "Technology"
        b.available = true
        return b
    }

    private BookRequest sampleRequest() {
        BookRequest r = new BookRequest()
        r.title = "Clean Code"
        r.author = "Robert Martin"
        r.description = "A handbook of agile software craftsmanship"
        r.price = new BigDecimal("29.99")
        r.genre = "Technology"
        r.available = true
        return r
    }

    // ── addBook ──────────────────────────────────────────────────────────────

    def "addBook saves and returns a BookResponse"() {
        given:
        Book saved = sampleBook()
        bookRepository.save(_ as Book) >> saved

        when:
        def response = bookService.addBook(sampleRequest())

        then:
        response.title == "Clean Code"
        response.author == "Robert Martin"
        response.price == new BigDecimal("29.99")
        response.available == true
        1 * bookRepository.save(_ as Book) >> saved
    }

    // ── getBookById ──────────────────────────────────────────────────────────

    def "getBookById returns response when book exists"() {
        given:
        bookRepository.findById(1L) >> Optional.of(sampleBook())

        when:
        def response = bookService.getBookById(1L)

        then:
        response.id == 1L
        response.title == "Clean Code"
    }

    def "getBookById throws ResourceNotFoundException when book is missing"() {
        given:
        bookRepository.findById(99L) >> Optional.empty()

        when:
        bookService.getBookById(99L)

        then:
        thrown(ResourceNotFoundException)
    }

    // ── getAllBooks ──────────────────────────────────────────────────────────

    def "getAllBooks returns mapped list"() {
        given:
        bookRepository.findAll() >> [sampleBook(1L), sampleBook(2L)]

        when:
        def result = bookService.getAllBooks()

        then:
        result.size() == 2
    }

    // ── getAvailableBooks ────────────────────────────────────────────────────

    def "getAvailableBooks delegates to findByAvailableTrue"() {
        given:
        bookRepository.findByAvailableTrue() >> [sampleBook()]

        when:
        def result = bookService.getAvailableBooks()

        then:
        result.size() == 1
        result[0].available == true
    }

    // ── searchByTitle ────────────────────────────────────────────────────────

    def "searchByTitle returns matching books"() {
        given:
        bookRepository.findByTitleContainingIgnoreCase("clean") >> [sampleBook()]

        when:
        def result = bookService.searchByTitle("clean")

        then:
        result.size() == 1
        result[0].title == "Clean Code"
    }

    // ── searchByAuthor ───────────────────────────────────────────────────────

    def "searchByAuthor returns matching books"() {
        given:
        bookRepository.findByAuthorContainingIgnoreCase("martin") >> [sampleBook()]

        when:
        def result = bookService.searchByAuthor("martin")

        then:
        result.size() == 1
        result[0].author == "Robert Martin"
    }

    // ── searchByGenre ────────────────────────────────────────────────────────

    def "searchByGenre returns matching books"() {
        given:
        bookRepository.findByGenreIgnoreCase("Technology") >> [sampleBook()]

        when:
        def result = bookService.searchByGenre("Technology")

        then:
        result.size() == 1
        result[0].genre == "Technology"
    }

    // ── updateBook ───────────────────────────────────────────────────────────

    def "updateBook modifies and persists the book"() {
        given:
        Book existing = sampleBook()
        bookRepository.findById(1L) >> Optional.of(existing)

        BookRequest updateRequest = new BookRequest()
        updateRequest.title = "The Clean Coder"
        updateRequest.author = "Robert Martin"
        updateRequest.description = "Professionalism in software"
        updateRequest.price = new BigDecimal("35.00")
        updateRequest.genre = "Technology"
        updateRequest.available = true

        when:
        def response = bookService.updateBook(1L, updateRequest)

        then:
        1 * bookRepository.save(_ as Book) >> { Book b -> b }
        response.title == "The Clean Coder"
        response.price == new BigDecimal("35.00")
    }

    def "updateBook throws ResourceNotFoundException when book is missing"() {
        given:
        bookRepository.findById(99L) >> Optional.empty()

        when:
        bookService.updateBook(99L, sampleRequest())

        then:
        thrown(ResourceNotFoundException)
    }

    // ── deleteBook ───────────────────────────────────────────────────────────

    def "deleteBook removes the book when it exists"() {
        given:
        bookRepository.existsById(1L) >> true

        when:
        bookService.deleteBook(1L)

        then:
        1 * bookRepository.deleteById(1L)
    }

    def "deleteBook throws ResourceNotFoundException when book is missing"() {
        given:
        bookRepository.existsById(99L) >> false

        when:
        bookService.deleteBook(99L)

        then:
        thrown(ResourceNotFoundException)
        0 * bookRepository.deleteById(_)
    }

    // ── getBookEntityById ────────────────────────────────────────────────────

    def "getBookEntityById returns the Book entity"() {
        given:
        Book book = sampleBook()
        bookRepository.findById(1L) >> Optional.of(book)

        when:
        def result = bookService.getBookEntityById(1L)

        then:
        result.is(book)
    }
}
