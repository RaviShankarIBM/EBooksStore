package com.ebookstore.service

import com.ebookstore.entity.Book
import com.ebookstore.entity.User
import com.ebookstore.entity.UserBook
import com.ebookstore.exception.ResourceAlreadyExistsException
import com.ebookstore.exception.ResourceNotFoundException
import com.ebookstore.repository.UserBookRepository
import com.ebookstore.repository.UserRepository
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime

class UserBookServiceSpec extends Specification {

    UserBookRepository userBookRepository = Mock()
    UserRepository userRepository = Mock()
    BookService bookService = Mock()

    @Subject
    UserBookService userBookService = new UserBookService(userBookRepository, userRepository, bookService)

    private User sampleUser(Long id = 1L) {
        User u = new User()
        u.id = id
        u.name = "Alice"
        u.email = "alice@example.com"
        u.password = "secret"
        return u
    }

    private Book sampleBook(Long id = 10L, boolean available = true) {
        Book b = new Book()
        b.id = id
        b.title = "Clean Code"
        b.author = "Robert Martin"
        b.price = new BigDecimal("29.99")
        b.genre = "Technology"
        b.available = available
        return b
    }

    private UserBook sampleUserBook(User user, Book book) {
        UserBook ub = new UserBook()
        ub.id = 100L
        ub.user = user
        ub.book = book
        ub.accessedAt = LocalDateTime.now()
        return ub
    }

    // ── accessBook ───────────────────────────────────────────────────────────

    def "accessBook creates and returns UserBookResponse"() {
        given:
        User user = sampleUser()
        Book book = sampleBook()
        UserBook saved = sampleUserBook(user, book)

        userRepository.findById(1L) >> Optional.of(user)
        bookService.getBookEntityById(10L) >> book
        userBookRepository.existsByUserIdAndBookId(1L, 10L) >> false

        when:
        def response = userBookService.accessBook(1L, 10L)

        then:
        1 * userBookRepository.save(_ as UserBook) >> saved
        response.userId == 1L
        response.userName == "Alice"
        response.bookId == 10L
        response.bookTitle == "Clean Code"
    }

    def "accessBook throws ResourceNotFoundException when user does not exist"() {
        given:
        userRepository.findById(99L) >> Optional.empty()

        when:
        userBookService.accessBook(99L, 10L)

        then:
        thrown(ResourceNotFoundException)
        0 * userBookRepository.save(_)
    }

    def "accessBook throws IllegalStateException when book is not available"() {
        given:
        User user = sampleUser()
        Book unavailableBook = sampleBook(10L, false)

        userRepository.findById(1L) >> Optional.of(user)
        bookService.getBookEntityById(10L) >> unavailableBook

        when:
        userBookService.accessBook(1L, 10L)

        then:
        thrown(IllegalStateException)
        0 * userBookRepository.save(_)
    }

    def "accessBook throws ResourceAlreadyExistsException when user already has book"() {
        given:
        User user = sampleUser()
        Book book = sampleBook()

        userRepository.findById(1L) >> Optional.of(user)
        bookService.getBookEntityById(10L) >> book
        userBookRepository.existsByUserIdAndBookId(1L, 10L) >> true

        when:
        userBookService.accessBook(1L, 10L)

        then:
        thrown(ResourceAlreadyExistsException)
        0 * userBookRepository.save(_)
    }

    // ── getBooksForUser ───────────────────────────────────────────────────────

    def "getBooksForUser returns list of user books"() {
        given:
        User user = sampleUser()
        Book book = sampleBook()
        UserBook ub = sampleUserBook(user, book)

        userRepository.existsById(1L) >> true
        userBookRepository.findByUserId(1L) >> [ub]

        when:
        def result = userBookService.getBooksForUser(1L)

        then:
        result.size() == 1
        result[0].bookTitle == "Clean Code"
        result[0].userName == "Alice"
    }

    def "getBooksForUser throws ResourceNotFoundException when user does not exist"() {
        given:
        userRepository.existsById(99L) >> false

        when:
        userBookService.getBooksForUser(99L)

        then:
        thrown(ResourceNotFoundException)
    }

    def "getBooksForUser returns empty list when user has no books"() {
        given:
        userRepository.existsById(1L) >> true
        userBookRepository.findByUserId(1L) >> []

        when:
        def result = userBookService.getBooksForUser(1L)

        then:
        result.isEmpty()
    }

    // ── removeAccess ──────────────────────────────────────────────────────────

    def "removeAccess deletes the UserBook record"() {
        given:
        User user = sampleUser()
        Book book = sampleBook()
        UserBook ub = sampleUserBook(user, book)

        userBookRepository.findByUserIdAndBookId(1L, 10L) >> Optional.of(ub)

        when:
        userBookService.removeAccess(1L, 10L)

        then:
        1 * userBookRepository.delete(ub)
    }

    def "removeAccess throws ResourceNotFoundException when record does not exist"() {
        given:
        userBookRepository.findByUserIdAndBookId(1L, 99L) >> Optional.empty()

        when:
        userBookService.removeAccess(1L, 99L)

        then:
        thrown(ResourceNotFoundException)
        0 * userBookRepository.delete(_)
    }
}
