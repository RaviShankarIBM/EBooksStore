package com.ebookstore.controller

import com.ebookstore.dto.UserBookResponse
import com.ebookstore.exception.GlobalExceptionHandler
import com.ebookstore.exception.ResourceAlreadyExistsException
import com.ebookstore.exception.ResourceNotFoundException
import com.ebookstore.service.UserBookService
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import java.time.LocalDateTime

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class UserBookControllerSpec extends Specification {

    UserBookService userBookService = Stub()
    UserBookController controller = new UserBookController(userBookService)
    MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build()

    private UserBookResponse sampleResponse() {
        new UserBookResponse(100L, 1L, "Alice", 10L, "Clean Code", "Robert Martin", LocalDateTime.now())
    }

    // ── POST /api/users/{userId}/books/{bookId} ───────────────────────────────

    def "POST /api/users/1/books/10 returns 201 and UserBookResponse"() {
        given:
        userBookService.accessBook(1L, 10L) >> sampleResponse()

        expect:
        mockMvc.perform(post("/api/users/1/books/10"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath('$.success').value(true))
                .andExpect(jsonPath('$.message').value("Book accessed successfully"))
                .andExpect(jsonPath('$.data.userId').value(1))
                .andExpect(jsonPath('$.data.bookTitle').value("Clean Code"))
    }

    def "POST /api/users/99/books/10 returns 404 when user not found"() {
        given:
        userBookService.accessBook(99L, 10L) >> { throw new ResourceNotFoundException("User not found with id: 99") }

        expect:
        mockMvc.perform(post("/api/users/99/books/10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath('$.success').value(false))
    }

    def "POST /api/users/1/books/10 returns 409 when user already has the book"() {
        given:
        userBookService.accessBook(1L, 10L) >> { throw new ResourceAlreadyExistsException("User already has access to this book") }

        expect:
        mockMvc.perform(post("/api/users/1/books/10"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath('$.success').value(false))
    }

    def "POST /api/users/1/books/10 returns 400 when book is not available"() {
        given:
        userBookService.accessBook(1L, 10L) >> { throw new IllegalStateException("Book 'Clean Code' is not available") }

        expect:
        mockMvc.perform(post("/api/users/1/books/10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.success').value(false))
    }

    // ── GET /api/users/{userId}/books ─────────────────────────────────────────

    def "GET /api/users/1/books returns list of user books"() {
        given:
        userBookService.getBooksForUser(1L) >> [sampleResponse()]

        expect:
        mockMvc.perform(get("/api/users/1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.success').value(true))
                .andExpect(jsonPath('$.data').isArray())
                .andExpect(jsonPath('$.data[0].bookTitle').value("Clean Code"))
                .andExpect(jsonPath('$.data[0].userName').value("Alice"))
    }

    def "GET /api/users/99/books returns 404 when user not found"() {
        given:
        userBookService.getBooksForUser(99L) >> { throw new ResourceNotFoundException("User not found with id: 99") }

        expect:
        mockMvc.perform(get("/api/users/99/books"))
                .andExpect(status().isNotFound())
    }

    def "GET /api/users/1/books returns empty list when user has no books"() {
        given:
        userBookService.getBooksForUser(1L) >> []

        expect:
        mockMvc.perform(get("/api/users/1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.data').isArray())
                .andExpect(jsonPath('$.data').isEmpty())
    }

    // ── DELETE /api/users/{userId}/books/{bookId} ─────────────────────────────

    def "DELETE /api/users/1/books/10 returns 200"() {
        given:
        userBookService.removeAccess(1L, 10L) >> null

        expect:
        mockMvc.perform(delete("/api/users/1/books/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.message').value("Book access removed"))
    }

    def "DELETE /api/users/1/books/99 returns 404 when record not found"() {
        given:
        userBookService.removeAccess(1L, 99L) >> { throw new ResourceNotFoundException("No access record found") }

        expect:
        mockMvc.perform(delete("/api/users/1/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath('$.success').value(false))
    }
}
