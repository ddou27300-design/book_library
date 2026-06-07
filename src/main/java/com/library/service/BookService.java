package com.library.service;

import com.library.dto.BookDto;
import com.library.entity.Book;
import com.library.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {
    Book addBook(BookDto dto);
    Book updateBook(Long id, BookDto dto);
    void deleteBook(Long id);
    Book findById(Long id);
    Page<Book> findAllActive(Pageable pageable);
    Page<Book> findByCategory(Category category, Pageable pageable);
    Page<Book> searchBooks(String keyword, Pageable pageable);
    Page<Book> searchBooksByCategory(String keyword, Category category, Pageable pageable);
    List<Book> findLatestBooks();
    List<Book> findTopDownloadedBooks();
    long countBooks();
    Long sumTotalDownloads();
    void incrementDownloads(Long bookId);
}