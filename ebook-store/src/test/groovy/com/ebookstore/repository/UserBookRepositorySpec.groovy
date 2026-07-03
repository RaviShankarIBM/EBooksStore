package com.ebookstore.repository

import com.ebookstore.EbookStoreApplication
import com.ebookstore.entity.Book
import com.ebookstore.entity.User
import com.ebookstore.entity.UserBook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@DataJpaTest
@ContextConfiguration(classes = EbookStoreApplication)
class UserBookRepositorySpec extends Specification {

    @Autowired
    UserBookRepository userBookRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    BookRepository bookRepository

    User savedUser
    Book savedBook

    def setup() {
        User user = new User()
        user.name = "Eve"
        user.email = "eve@example.com"
        user.password = "pass"
        savedUser = userRepository.save(user)

        Book book = new Book()
        book.title = "DDD"
        book.author = "Eric Evans"
        book.price = new BigDecimal("35.00")
        book.genre = "Technology"
        book.available = true
        savedBook = bookRepository.save(book)
    }

    private UserBook linkUserBook(User user, Book book) {
        UserBook ub = new UserBook()
        ub.user = user
        ub.book = book
        return userBookRepository.save(ub)
    }

    def "save a UserBook and find by id"() {
        when:
        UserBook ub = linkUserBook(savedUser, savedBook)

        then:
        userBookRepository.findById(ub.id).isPresent()
    }

    def "findByUserId returns books for the user"() {
        given:
        linkUserBook(savedUser, savedBook)

        when:
        def result = userBookRepository.findByUserId(savedUser.id)

        then:
        result.size() == 1
        result[0].book.title == "DDD"
    }

    def "findByUserIdAndBookId returns the correct record"() {
        given:
        linkUserBook(savedUser, savedBook)

        when:
        def result = userBookRepository.findByUserIdAndBookId(savedUser.id, savedBook.id)

        then:
        result.isPresent()
        result.get().user.id == savedUser.id
        result.get().book.id == savedBook.id
    }

    def "existsByUserIdAndBookId returns true when record exists"() {
        given:
        linkUserBook(savedUser, savedBook)

        expect:
        userBookRepository.existsByUserIdAndBookId(savedUser.id, savedBook.id)
    }

    def "existsByUserIdAndBookId returns false when record does not exist"() {
        expect:
        !userBookRepository.existsByUserIdAndBookId(savedUser.id, savedBook.id)
    }

    def "delete removes the UserBook record"() {
        given:
        UserBook ub = linkUserBook(savedUser, savedBook)

        when:
        userBookRepository.delete(ub)

        then:
        !userBookRepository.existsByUserIdAndBookId(savedUser.id, savedBook.id)
    }
}
