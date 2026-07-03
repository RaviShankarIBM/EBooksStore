package com.ebookstore.controller;

import com.ebookstore.dto.ApiResponse;
import com.ebookstore.dto.BookRequest;
import com.ebookstore.dto.BookResponse;
import com.ebookstore.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * POST /api/books
     * Add a new book to the store.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BookResponse>> addBook(@Valid @RequestBody BookRequest request) {
        BookResponse response = bookService.addBook(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Book added successfully", response));
    }

    /**
     * GET /api/books
     * Get all books. Optional query params: ?title=, ?author=, ?genre=
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BookResponse>>> getBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String genre) {

        List<BookResponse> books;
        if (title != null) {
            books = bookService.searchByTitle(title);
        } else if (author != null) {
            books = bookService.searchByAuthor(author);
        } else if (genre != null) {
            books = bookService.searchByGenre(genre);
        } else {
            books = bookService.getAllBooks();
        }
        return ResponseEntity.ok(ApiResponse.success("Books retrieved", books));
    }

    /**
     * GET /api/books/available
     * Get all available books.
     */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getAvailableBooks() {
        return ResponseEntity.ok(ApiResponse.success("Available books retrieved", bookService.getAvailableBooks()));
    }

    /**
     * GET /api/books/{id}
     * Get a specific book by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Book retrieved", bookService.getBookById(id)));
    }

    /**
     * PUT /api/books/{id}
     * Update a book.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequest request) {
        BookResponse response = bookService.updateBook(id, request);
        return ResponseEntity.ok(ApiResponse.success("Book updated successfully", response));
    }

    /**
     * DELETE /api/books/{id}
     * Remove a book from the store.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok(ApiResponse.success("Book deleted successfully", null));
    }
}
