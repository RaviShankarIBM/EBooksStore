package com.ebookstore.controller;

import com.ebookstore.dto.ApiResponse;
import com.ebookstore.dto.UserBookResponse;
import com.ebookstore.service.UserBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/books")
@RequiredArgsConstructor
public class UserBookController {

    private final UserBookService userBookService;

    /**
     * POST /api/users/{userId}/books/{bookId}
     * Give a user access to a book from the store.
     */
    @PostMapping("/{bookId}")
    public ResponseEntity<ApiResponse<UserBookResponse>> accessBook(
            @PathVariable Long userId,
            @PathVariable Long bookId) {
        UserBookResponse response = userBookService.accessBook(userId, bookId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Book accessed successfully", response));
    }

    /**
     * GET /api/users/{userId}/books
     * Get all books accessed/owned by a user.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserBookResponse>>> getUserBooks(@PathVariable Long userId) {
        List<UserBookResponse> books = userBookService.getBooksForUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User books retrieved", books));
    }

    /**
     * DELETE /api/users/{userId}/books/{bookId}
     * Remove a book from a user's library.
     */
    @DeleteMapping("/{bookId}")
    public ResponseEntity<ApiResponse<Void>> removeAccess(
            @PathVariable Long userId,
            @PathVariable Long bookId) {
        userBookService.removeAccess(userId, bookId);
        return ResponseEntity.ok(ApiResponse.success("Book access removed", null));
    }
}
