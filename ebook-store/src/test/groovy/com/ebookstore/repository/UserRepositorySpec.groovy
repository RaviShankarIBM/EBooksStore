package com.ebookstore.repository

import com.ebookstore.EbookStoreApplication
import com.ebookstore.entity.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@DataJpaTest
@ContextConfiguration(classes = EbookStoreApplication)
class UserRepositorySpec extends Specification {

    @Autowired
    UserRepository userRepository

    private User makeUser(String name, String email) {
        User u = new User()
        u.name = name
        u.email = email
        u.password = "secret123"
        return u
    }

    def "save and find user by id"() {
        given:
        User user = userRepository.save(makeUser("Alice", "alice@example.com"))

        when:
        def found = userRepository.findById(user.id)

        then:
        found.isPresent()
        found.get().name == "Alice"
        found.get().email == "alice@example.com"
    }

    def "findByEmail returns user when email exists"() {
        given:
        userRepository.save(makeUser("Bob", "bob@example.com"))

        when:
        def result = userRepository.findByEmail("bob@example.com")

        then:
        result.isPresent()
        result.get().name == "Bob"
    }

    def "findByEmail returns empty when email does not exist"() {
        when:
        def result = userRepository.findByEmail("nonexistent@example.com")

        then:
        !result.isPresent()
    }

    def "existsByEmail returns true when email is registered"() {
        given:
        userRepository.save(makeUser("Carol", "carol@example.com"))

        expect:
        userRepository.existsByEmail("carol@example.com")
    }

    def "existsByEmail returns false when email is not registered"() {
        expect:
        !userRepository.existsByEmail("nobody@example.com")
    }

    def "deleteById removes the user"() {
        given:
        User user = userRepository.save(makeUser("Dave", "dave@example.com"))

        when:
        userRepository.deleteById(user.id)

        then:
        !userRepository.existsById(user.id)
    }
}
