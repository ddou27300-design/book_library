package com.library.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    private String pdfFileUrl;

    private String coverImageUrl;

    private String isbn;

    private Integer publishYear;

    private String publisher;

    private String language;

    private Long totalDownloads = 0L;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Favorite> favorites;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DownloadHistory> downloadHistories;

    public Book() {}

    public Book(Long id, String title, String author, String description, Category category,
                String pdfFileUrl, String coverImageUrl, String isbn, Integer publishYear,
                String publisher, String language, Long totalDownloads, boolean active,
                LocalDateTime createdAt, List<Favorite> favorites, List<DownloadHistory> downloadHistories) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.description = description;
        this.category = category;
        this.pdfFileUrl = pdfFileUrl;
        this.coverImageUrl = coverImageUrl;
        this.isbn = isbn;
        this.publishYear = publishYear;
        this.publisher = publisher;
        this.language = language;
        this.totalDownloads = totalDownloads;
        this.active = active;
        this.createdAt = createdAt;
        this.favorites = favorites;
        this.downloadHistories = downloadHistories;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String title;
        private String author;
        private String description;
        private Category category;
        private String pdfFileUrl;
        private String coverImageUrl;
        private String isbn;
        private Integer publishYear;
        private String publisher;
        private String language;
        private Long totalDownloads = 0L;
        private boolean active = true;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder author(String author) { this.author = author; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder category(Category category) { this.category = category; return this; }
        public Builder pdfFileUrl(String pdfFileUrl) { this.pdfFileUrl = pdfFileUrl; return this; }
        public Builder coverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; return this; }
        public Builder isbn(String isbn) { this.isbn = isbn; return this; }
        public Builder publishYear(Integer publishYear) { this.publishYear = publishYear; return this; }
        public Builder publisher(String publisher) { this.publisher = publisher; return this; }
        public Builder language(String language) { this.language = language; return this; }
        public Builder totalDownloads(Long totalDownloads) { this.totalDownloads = totalDownloads; return this; }
        public Builder active(boolean active) { this.active = active; return this; }

        public Book build() {
            Book book = new Book();
            book.id = this.id;
            book.title = this.title;
            book.author = this.author;
            book.description = this.description;
            book.category = this.category;
            book.pdfFileUrl = this.pdfFileUrl;
            book.coverImageUrl = this.coverImageUrl;
            book.isbn = this.isbn;
            book.publishYear = this.publishYear;
            book.publisher = this.publisher;
            book.language = this.language;
            book.totalDownloads = this.totalDownloads;
            book.active = this.active;
            return book;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public String getPdfFileUrl() { return pdfFileUrl; }
    public void setPdfFileUrl(String pdfFileUrl) { this.pdfFileUrl = pdfFileUrl; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public Integer getPublishYear() { return publishYear; }
    public void setPublishYear(Integer publishYear) { this.publishYear = publishYear; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public Long getTotalDownloads() { return totalDownloads; }
    public void setTotalDownloads(Long totalDownloads) { this.totalDownloads = totalDownloads; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<Favorite> getFavorites() { return favorites; }
    public void setFavorites(List<Favorite> favorites) { this.favorites = favorites; }
    public List<DownloadHistory> getDownloadHistories() { return downloadHistories; }
    public void setDownloadHistories(List<DownloadHistory> downloadHistories) { this.downloadHistories = downloadHistories; }
}
