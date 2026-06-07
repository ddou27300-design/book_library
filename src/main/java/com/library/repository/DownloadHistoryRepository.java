package com.library.repository;

import com.library.entity.Book;
import com.library.entity.DownloadHistory;
import com.library.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DownloadHistoryRepository extends JpaRepository<DownloadHistory, Long> {
    Page<DownloadHistory> findByUserOrderByDownloadDateDesc(User user, Pageable pageable);
    List<DownloadHistory> findTop10ByOrderByDownloadDateDesc();

    @Query("SELECT COUNT(d) FROM DownloadHistory d WHERE d.downloadDate >= :since")
    long countDownloadsSince(LocalDateTime since);

    @Query("SELECT d.book.title, COUNT(d) as cnt FROM DownloadHistory d GROUP BY d.book ORDER BY cnt DESC")
    List<Object[]> findTopDownloadedBooks(Pageable pageable);

    @Query("SELECT FUNCTION('DATE', d.downloadDate) as day, COUNT(d) FROM DownloadHistory d WHERE d.downloadDate >= :since GROUP BY FUNCTION('DATE', d.downloadDate) ORDER BY day")
    List<Object[]> countDownloadsPerDay(LocalDateTime since);
}