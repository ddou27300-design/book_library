package com.library.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "visitor_logs")
public class VisitorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ipAddress;

    private String userAgent;

    private String page;

    @CreationTimestamp
    private LocalDateTime visitDate;

    public VisitorLog() {}

    public VisitorLog(Long id, String ipAddress, String userAgent, String page, LocalDateTime visitDate) {
        this.id = id;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.page = page;
        this.visitDate = visitDate;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String ipAddress;
        private String userAgent;
        private String page;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
        public Builder userAgent(String userAgent) { this.userAgent = userAgent; return this; }
        public Builder page(String page) { this.page = page; return this; }

        public VisitorLog build() {
            VisitorLog v = new VisitorLog();
            v.id = this.id;
            v.ipAddress = this.ipAddress;
            v.userAgent = this.userAgent;
            v.page = this.page;
            return v;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public String getPage() { return page; }
    public void setPage(String page) { this.page = page; }
    public LocalDateTime getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDateTime visitDate) { this.visitDate = visitDate; }
}
