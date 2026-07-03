package com.ebookstore.service

import com.ebookstore.dto.UserRegistrationRequest
import com.ebookstore.entity.User
import com.ebookstore.exception.ResourceAlreadyExistsException
import com.ebookstore.exception.ResourceNotFoundException
import com.ebookstore.repository.UserRepository
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime

class UserServiceSpec extends Specification {

    UserRepository userRepository = Mock()

    @Subject
    UserService userService = new UserService(userRepository)

    private User sampleUser(Long id = 1L) {
        User u = new User()
        u.id = id
        u.name = "Alice"
        u.email = "alice@example.com"
        u.password = "secret"
        u.registeredAt = LocalDateTime.now()
        return u
    }

    private UserRegistrationRequest sampleRequest() {
        UserRegistrationRequest r = new UserRegistrationRequest()
        r.name = "Alice"
        r.email = "alice@example.com"
        r.password = "secret"
        return r
    }

    // ── registerUser ─────────────────────────────────────────────────────────

    def "registerUser saves and returns a UserResponse"() {
        given:
        User saved = sampleUser()
        userRepository.existsByEmail("alice@example.com") >> false

        when:
        def response = userService.registerUser(sampleRequest())

        then:
        1 * userRepository.save(_ as User) >> saved
        response.name == "Alice"
        response.email == "alice@example.com"
    }

    def "registerUser throws ResourceAlreadyExistsException when email is taken"() {
        given:
        userRepository.existsByEmail("alice@example.com") >> true

        when:
        userService.registerUser(sampleRequest())

        then:
        thrown(ResourceAlreadyExistsException)
        0 * userRepository.save(_)
    }

    // ── getUserById ──────────────────────────────────────────────────────────

    def "getUserById returns response when user exists"() {
        given:
        userRepository.findById(1L) >> Optional.of(sampleUser())

        when:
        def response = userService.getUserById(1L)

        then:
        response.id == 1L
        response.name == "Alice"
    }

    def "getUserById throws ResourceNotFoundException when user is missing"() {
        given:
        userRepository.findById(99L) >> Optional.empty()

        when:
        userService.getUserById(99L)

        then:
        thrown(ResourceNotFoundException)
    }

    // ── getUserByEmail ───────────────────────────────────────────────────────

    def "getUserByEmail returns response when email exists"() {
        given:
        userRepository.findByEmail("alice@example.com") >> Optional.of(sampleUser())

        when:
        def response = userService.getUserByEmail("alice@example.com")

        then:
        response.email == "alice@example.com"
    }

    def "getUserByEmail throws ResourceNotFoundException when email is missing"() {
        given:
        userRepository.findByEmail("ghost@example.com") >> Optional.empty()

        when:
        userService.getUserByEmail("ghost@example.com")

        then:
        thrown(ResourceNotFoundException)
    }

    // ── getAllUsers ──────────────────────────────────────────────────────────

    def "getAllUsers returns all users"() {
        given:
        userRepository.findAll() >> [sampleUser(1L), sampleUser(2L)]

        when:
        def result = userService.getAllUsers()

        then:
        result.size() == 2
    }

    def "getAllUsers returns empty list when no users exist"() {
        given:
        userRepository.findAll() >> []

        when:
        def result = userService.getAllUsers()

        then:
        result.isEmpty()
    }

    // ── deleteUser ───────────────────────────────────────────────────────────

    def "deleteUser deletes when user exists"() {
        given:
        userRepository.existsById(1L) >> true

        when:
        userService.deleteUser(1L)

        then:
        1 * userRepository.deleteById(1L)
    }

    def "deleteUser throws ResourceNotFoundException when user is missing"() {
        given:
        userRepository.existsById(99L) >> false

        when:
        userService.deleteUser(99L)

        then:
        thrown(ResourceNotFoundException)
        0 * userRepository.deleteById(_)
    }
}
