package com.ebookstore.config;

import com.ebookstore.entity.Book;
import com.ebookstore.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    @Bean
    CommandLineRunner seedBooks(BookRepository bookRepository) {
        return args -> {
            if (bookRepository.count() == 0) {
                bookRepository.saveAll(List.of(
                        createBook("Clean Code", "Robert C. Martin",
                                "A handbook of agile software craftsmanship",
                                new BigDecimal("29.99"), "Technology"),
                        createBook("The Pragmatic Programmer", "David Thomas",
                                "From journeyman to master",
                                new BigDecimal("34.99"), "Technology"),
                        createBook("Design Patterns", "Gang of Four",
                                "Elements of reusable object-oriented software",
                                new BigDecimal("39.99"), "Technology"),
                        createBook("Thinking, Fast and Slow", "Daniel Kahneman",
                                "Exploring the two systems that drive the way we think",
                                new BigDecimal("19.99"), "Psychology"),
                        createBook("Sapiens", "Yuval Noah Harari",
                                "A brief history of humankind",
                                new BigDecimal("24.99"), "History"),
                        createBook("The Great Gatsby", "F. Scott Fitzgerald",
                                "A story of the fabulously wealthy Jay Gatsby",
                                new BigDecimal("9.99"), "Fiction")
                ));
                System.out.println(">>> Sample books seeded into database.");
            }
        };
    }

    private Book createBook(String title, String author, String description,
                            BigDecimal price, String genre) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setDescription(description);
        book.setPrice(price);
        book.setGenre(genre);
        book.setAvailable(true);
        return book;
    }
}
