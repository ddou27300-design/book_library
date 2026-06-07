package com.library.repository;

import com.library.entity.VisitorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VisitorLogRepository extends JpaRepository<VisitorLog, Long> {
    long countByVisitDateAfter(LocalDateTime date);

    @Query("SELECT FUNCTION('DATE', v.visitDate) as day, COUNT(v) FROM VisitorLog v WHERE v.visitDate >= :since GROUP BY FUNCTION('DATE', v.visitDate) ORDER BY day")
    List<Object[]> countVisitorsPerDay(LocalDateTime since);
}