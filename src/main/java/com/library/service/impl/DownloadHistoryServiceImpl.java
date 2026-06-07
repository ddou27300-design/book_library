package com.library.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.entity.Book;
import com.library.entity.DownloadHistory;
import com.library.entity.User;
import com.library.repository.DownloadHistoryRepository;
import com.library.service.DownloadHistoryService;

@Service
public class DownloadHistoryServiceImpl implements DownloadHistoryService {

    private final DownloadHistoryRepository downloadHistoryRepository;

    // Standard Java constructor injection replacing @RequiredArgsConstructor
    public DownloadHistoryServiceImpl(DownloadHistoryRepository downloadHistoryRepository) {
        this.downloadHistoryRepository = downloadHistoryRepository;
    }

    @Override
    @Transactional
    public void recordDownload(User user, Book book, String ipAddress) {
        DownloadHistory history = DownloadHistory.builder()
                .user(user)
                .book(book)
                .ipAddress(ipAddress)
                .build();
        downloadHistoryRepository.save(history);
    }

    @Override
    public Page<DownloadHistory> getUserDownloadHistory(User user, Pageable pageable) {
        return downloadHistoryRepository.findByUserOrderByDownloadDateDesc(user, pageable);
    }

    @Override
    public List<DownloadHistory> getRecentDownloads() {
        return downloadHistoryRepository.findTop10ByOrderByDownloadDateDesc();
    }

    @Override
    public long countTodayDownloads() {
        return downloadHistoryRepository.countDownloadsSince(LocalDateTime.now().withHour(0).withMinute(0));
    }

    @Override
    public List<Object[]> getDownloadsPerDay(int days) {
        return downloadHistoryRepository.countDownloadsPerDay(LocalDateTime.now().minusDays(days));
    }
}