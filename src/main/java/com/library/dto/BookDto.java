package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class BookDto {

    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    private String description;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private String isbn;

    private Integer publishYear;

    private String publisher;

    private String language;

    private MultipartFile pdfFile;

    private MultipartFile coverFile;

    private String existingPdfPath;

    private String existingCoverImage;

    public BookDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public Integer getPublishYear() { return publishYear; }
    public void setPublishYear(Integer publishYear) { this.publishYear = publishYear; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public MultipartFile getPdfFile() { return pdfFile; }
    public void setPdfFile(MultipartFile pdfFile) { this.pdfFile = pdfFile; }
    public MultipartFile getCoverFile() { return coverFile; }
    public void setCoverFile(MultipartFile coverFile) { this.coverFile = coverFile; }
    public String getExistingPdfPath() { return existingPdfPath; }
    public void setExistingPdfPath(String existingPdfPath) { this.existingPdfPath = existingPdfPath; }
    public String getExistingCoverImage() { return existingCoverImage; }
    public void setExistingCoverImage(String existingCoverImage) { this.existingCoverImage = existingCoverImage; }
}
