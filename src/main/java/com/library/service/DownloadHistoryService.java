package com.library.service;

import com.library.entity.Book;
import com.library.entity.DownloadHistory;
import com.library.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DownloadHistoryService {
    void recordDownload(User user, Book book, String ipAddress);
    Page<DownloadHistory> getUserDownloadHistory(User user, Pageable pageable);
    List<DownloadHistory> getRecentDownloads();
    long countTodayDownloads();
    List<Object[]> getDownloadsPerDay(int days);
}