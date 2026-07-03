package com.ebookstore.controller

import com.ebookstore.dto.BookRequest
import com.ebookstore.dto.BookResponse
import com.ebookstore.exception.GlobalExceptionHandler
import com.ebookstore.exception.ResourceNotFoundException
import com.ebookstore.service.BookService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class BookControllerSpec extends Specification {

    BookService bookService = Stub()
    BookController controller = new BookController(bookService)
    MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build()

    ObjectMapper objectMapper = new ObjectMapper()

    private BookResponse sampleResponse() {
        new BookResponse(1L, "Clean Code", "Robert Martin", "Craftsmanship", new BigDecimal("29.99"), "Technology", true)
    }

    private static final String VALID_BOOK_JSON =
            '{"title":"Clean Code","author":"Robert Martin","description":"Craftsmanship","price":29.99,"genre":"Technology","available":true}'

    // ── POST /api/books ───────────────────────────────────────────────────────

    def "POST /api/books returns 201 and BookResponse"() {
        given:
        bookService.addBook(_ as BookRequest) >> sampleResponse()

        expect:
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BOOK_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath('$.success').value(true))
                .andExpect(jsonPath('$.data.title').value("Clean Code"))
                .andExpect(jsonPath('$.data.author').value("Robert Martin"))
    }

    def "POST /api/books returns 400 when title is missing"() {
        given:
        def missingTitle = '{"author":"Robert Martin","price":29.99,"genre":"Technology"}'

        expect:
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(missingTitle))
                .andExpect(status().isBadRequest())
    }

    // ── GET /api/books ────────────────────────────────────────────────────────

    def "GET /api/books returns all books"() {
        given:
        bookService.getAllBooks() >> [sampleResponse()]

        expect:
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.data').isArray())
                .andExpect(jsonPath('$.data[0].title').value("Clean Code"))
    }

    def "GET /api/books?title=clean delegates to searchByTitle"() {
        given:
        bookService.searchByTitle("clean") >> [sampleResponse()]

        expect:
        mockMvc.perform(get("/api/books").param("title", "clean"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.data[0].title').value("Clean Code"))
    }

    def "GET /api/books?author=martin delegates to searchByAuthor"() {
        given:
        bookService.searchByAuthor("martin") >> [sampleResponse()]

        expect:
        mockMvc.perform(get("/api/books").param("author", "martin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.data[0].author').value("Robert Martin"))
    }

    def "GET /api/books?genre=technology delegates to searchByGenre"() {
        given:
        bookService.searchByGenre("technology") >> [sampleResponse()]

        expect:
        mockMvc.perform(get("/api/books").param("genre", "technology"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.data[0].genre').value("Technology"))
    }

    // ── GET /api/books/available ──────────────────────────────────────────────

    def "GET /api/books/available returns available books"() {
        given:
        bookService.getAvailableBooks() >> [sampleResponse()]

        expect:
        mockMvc.perform(get("/api/books/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.data[0].available').value(true))
    }

    // ── GET /api/books/{id} ───────────────────────────────────────────────────

    def "GET /api/books/1 returns the book"() {
        given:
        bookService.getBookById(1L) >> sampleResponse()

        expect:
        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.data.id').value(1))
    }

    def "GET /api/books/99 returns 404 when book not found"() {
        given:
        bookService.getBookById(99L) >> { throw new ResourceNotFoundException("Book not found with id: 99") }

        expect:
        mockMvc.perform(get("/api/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath('$.success').value(false))
    }

    // ── PUT /api/books/{id} ───────────────────────────────────────────────────

    def "PUT /api/books/1 updates and returns the book"() {
        given:
        def updated = new BookResponse(1L, "The Clean Coder", "Robert Martin", "Professionalism", new BigDecimal("35.00"), "Technology", true)
        bookService.updateBook(1L, _ as BookRequest) >> updated

        def updateJson = '{"title":"The Clean Coder","author":"Robert Martin","description":"Professionalism","price":35.00,"genre":"Technology","available":true}'

        expect:
        mockMvc.perform(put("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.data.title').value("The Clean Coder"))
    }

    def "PUT /api/books/99 returns 404 when book not found"() {
        given:
        bookService.updateBook(99L, _ as BookRequest) >> { throw new ResourceNotFoundException("Book not found with id: 99") }

        expect:
        mockMvc.perform(put("/api/books/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BOOK_JSON))
                .andExpect(status().isNotFound())
    }

    // ── DELETE /api/books/{id} ────────────────────────────────────────────────

    def "DELETE /api/books/1 returns 200"() {
        given:
        bookService.deleteBook(1L) >> null

        expect:
        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.message').value("Book deleted successfully"))
    }

    def "DELETE /api/books/99 returns 404 when book not found"() {
        given:
        bookService.deleteBook(99L) >> { throw new ResourceNotFoundException("Book not found with id: 99") }

        expect:
        mockMvc.perform(delete("/api/books/99"))
                .andExpect(status().isNotFound())
    }
}
