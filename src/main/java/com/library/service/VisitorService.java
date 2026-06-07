package com.library.service;

import com.library.entity.VisitorLog;
import com.library.repository.VisitorLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VisitorService {

    private final VisitorLogRepository visitorLogRepository;

    public VisitorService(VisitorLogRepository visitorLogRepository) {
        this.visitorLogRepository = visitorLogRepository;
    }

    public void logVisit(String ipAddress, String userAgent, String page) {
        VisitorLog log = VisitorLog.builder()
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .page(page)
                .build();
        visitorLogRepository.save(log);
    }

    public long countTotalVisitors() {
        return visitorLogRepository.count();
    }

    public long countTodayVisitors() {
        return visitorLogRepository.countByVisitDateAfter(LocalDateTime.now().withHour(0).withMinute(0));
    }

    public List<Object[]> getVisitorsPerDay(int days) {
        return visitorLogRepository.countVisitorsPerDay(LocalDateTime.now().minusDays(days));
    }
}
