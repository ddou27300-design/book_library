package com.library.repository;

import com.library.entity.Book;
import com.library.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Page<Book> findByActiveTrue(Pageable pageable);
    Page<Book> findByCategoryAndActiveTrue(Category category, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.active = true AND (LOWER(b.title) LIKE LOWER(CONCAT('%',:keyword,'%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%',:keyword,'%')))")
    Page<Book> searchBooks(String keyword, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.active = true AND (LOWER(b.title) LIKE LOWER(CONCAT('%',:keyword,'%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%',:keyword,'%'))) AND b.category = :category")
    Page<Book> searchBooksByCategory(String keyword, Category category, Pageable pageable);

    List<Book> findTop8ByActiveTrueOrderByCreatedAtDesc();
    List<Book> findTop8ByActiveTrueOrderByTotalDownloadsDesc();

    long countByActiveTrue();

    @Query("SELECT SUM(b.totalDownloads) FROM Book b")
    Long sumTotalDownloads();
}