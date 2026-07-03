package com.ebookstore.service;

import com.ebookstore.dto.UserBookResponse;
import com.ebookstore.entity.Book;
import com.ebookstore.entity.User;
import com.ebookstore.entity.UserBook;
import com.ebookstore.exception.ResourceAlreadyExistsException;
import com.ebookstore.exception.ResourceNotFoundException;
import com.ebookstore.repository.UserBookRepository;
import com.ebookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserBookService {

    private final UserBookRepository userBookRepository;
    private final UserRepository userRepository;
    private final BookService bookService;

    @Transactional
    public UserBookResponse accessBook(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Book book = bookService.getBookEntityById(bookId);

        if (!book.isAvailable()) {
            throw new IllegalStateException("Book '" + book.getTitle() + "' is not available");
        }

        if (userBookRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new ResourceAlreadyExistsException("User already has access to this book");
        }

        UserBook userBook = new UserBook();
        userBook.setUser(user);
        userBook.setBook(book);
        UserBook saved = userBookRepository.save(userBook);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<UserBookResponse> getBooksForUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return userBookRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeAccess(Long userId, Long bookId) {
        UserBook userBook = userBookRepository.findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No access record found for userId=" + userId + " and bookId=" + bookId));
        userBookRepository.delete(userBook);
    }

    private UserBookResponse toResponse(UserBook ub) {
        return new UserBookResponse(
                ub.getId(),
                ub.getUser().getId(),
                ub.getUser().getName(),
                ub.getBook().getId(),
                ub.getBook().getTitle(),
                ub.getBook().getAuthor(),
                ub.getAccessedAt()
        );
    }
}
