package com.ebookstore.controller

import com.ebookstore.dto.UserRegistrationRequest
import com.ebookstore.dto.UserResponse
import com.ebookstore.exception.GlobalExceptionHandler
import com.ebookstore.exception.ResourceAlreadyExistsException
import com.ebookstore.exception.ResourceNotFoundException
import com.ebookstore.service.UserService
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import java.time.LocalDateTime

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class UserControllerSpec extends Specification {

    UserService userService = Stub()
    UserController controller = new UserController(userService)
    MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build()

    private UserResponse sampleResponse(Long id = 1L) {
        new UserResponse(id, "Alice", "alice@example.com", LocalDateTime.now())
    }

    private static final String VALID_USER_JSON =
            '{"name":"Alice","email":"alice@example.com","password":"secret123"}'

    // ── POST /api/users/register ──────────────────────────────────────────────

    def "POST /api/users/register returns 201 and UserResponse"() {
        given:
        userService.registerUser(_ as UserRegistrationRequest) >> sampleResponse()

        expect:
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_USER_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath('$.success').value(true))
                .andExpect(jsonPath('$.data.name').value("Alice"))
                .andExpect(jsonPath('$.data.email').value("alice@example.com"))
    }

    def "POST /api/users/register returns 409 when email already exists"() {
        given:
        userService.registerUser(_ as UserRegistrationRequest) >> {
            throw new ResourceAlreadyExistsException("User already exists")
        }

        expect:
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_USER_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath('$.success').value(false))
    }

    def "POST /api/users/register returns 400 when email is invalid"() {
        given:
        def invalidEmail = '{"name":"Alice","email":"not-an-email","password":"secret123"}'

        expect:
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidEmail))
                .andExpect(status().isBadRequest())
    }

    // ── GET /api/users ────────────────────────────────────────────────────────

    def "GET /api/users returns all users"() {
        given:
        userService.getAllUsers() >> [sampleResponse(1L), sampleResponse(2L)]

        expect:
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.data').isArray())
                .andExpect(jsonPath('$.data.length()').value(2))
    }

    // ── GET /api/users/{id} ───────────────────────────────────────────────────

    def "GET /api/users/1 returns the user"() {
        given:
        userService.getUserById(1L) >> sampleResponse()

        expect:
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.data.id').value(1))
                .andExpect(jsonPath('$.data.name').value("Alice"))
    }

    def "GET /api/users/99 returns 404 when user not found"() {
        given:
        userService.getUserById(99L) >> { throw new ResourceNotFoundException("User not found with id: 99") }

        expect:
        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath('$.success').value(false))
    }

    // ── GET /api/users/email/{email} ──────────────────────────────────────────

    def "GET /api/users/email/alice returns the user"() {
        given:
        userService.getUserByEmail("alice@example.com") >> sampleResponse()

        expect:
        mockMvc.perform(get("/api/users/email/alice@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.data.email').value("alice@example.com"))
    }

    def "GET /api/users/email/ghost returns 404 when user not found"() {
        given:
        userService.getUserByEmail("ghost@example.com") >> { throw new ResourceNotFoundException("User not found") }

        expect:
        mockMvc.perform(get("/api/users/email/ghost@example.com"))
                .andExpect(status().isNotFound())
    }

    // ── DELETE /api/users/{id} ────────────────────────────────────────────────

    def "DELETE /api/users/1 returns 200"() {
        given:
        userService.deleteUser(1L) >> null

        expect:
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.message').value("User deleted successfully"))
    }

    def "DELETE /api/users/99 returns 404 when user not found"() {
        given:
        userService.deleteUser(99L) >> { throw new ResourceNotFoundException("User not found with id: 99") }

        expect:
        mockMvc.perform(delete("/api/users/99"))
                .andExpect(status().isNotFound())
    }
}
