package com.library.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.library.dto.BookDto;
import com.library.entity.Book;
import com.library.entity.Category;
import com.library.repository.BookRepository;
import com.library.repository.CategoryRepository;
import com.library.service.BookService;
import com.library.service.FileStorageService;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;

    // Standard Java constructor injection replacing @RequiredArgsConstructor
    public BookServiceImpl(BookRepository bookRepository, 
                    CategoryRepository categoryRepository, 
                    FileStorageService fileStorageService) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional
    public Book addBook(BookDto dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Book book = Book.builder()
                .title(dto.getTitle())
                .author(dto.getAuthor())
                .description(dto.getDescription())
                .category(category)
                .isbn(dto.getIsbn())
                .publishYear(dto.getPublishYear())
                .publisher(dto.getPublisher())
                .language(dto.getLanguage())
                .totalDownloads(0L)
                .active(true)
                .build();

        // Store PDF
        MultipartFile pdfFile = dto.getPdfFile();
        if (pdfFile != null && !pdfFile.isEmpty()) {
            String pdfPath = fileStorageService.storeBookPdf(pdfFile);
            book.setPdfPath(pdfPath);
        }

        // Store cover image
        MultipartFile coverFile = dto.getCoverFile();
        if (coverFile != null && !coverFile.isEmpty()) {
            String coverPath = fileStorageService.storeCoverImage(coverFile);
            book.setCoverImage(coverPath);
        }

        return bookRepository.save(book);
    }

    @Override
    @Transactional
    public Book updateBook(Long id, BookDto dto) {
        Book book = findById(id);
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setDescription(dto.getDescription());
        book.setCategory(category);
        book.setIsbn(dto.getIsbn());
        book.setPublishYear(dto.getPublishYear());
        book.setPublisher(dto.getPublisher());
        book.setLanguage(dto.getLanguage());

        // Update PDF if new one uploaded
        MultipartFile pdfFile = dto.getPdfFile();
        if (pdfFile != null && !pdfFile.isEmpty()) {
            if (book.getPdfPath() != null) fileStorageService.deleteFile(book.getPdfPath());
            book.setPdfPath(fileStorageService.storeBookPdf(pdfFile));
        }

        // Update cover if new one uploaded
        MultipartFile coverFile = dto.getCoverFile();
        if (coverFile != null && !coverFile.isEmpty()) {
            if (book.getCoverImage() != null) fileStorageService.deleteFile(book.getCoverImage());
            book.setCoverImage(fileStorageService.storeCoverImage(coverFile));
        }

        return bookRepository.save(book);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        Book book = findById(id);
        if (book.getPdfPath() != null) fileStorageService.deleteFile(book.getPdfPath());
        if (book.getCoverImage() != null) fileStorageService.deleteFile(book.getCoverImage());
        bookRepository.delete(book);
    }

    @Override
    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
    }

    @Override
    public Page<Book> findAllActive(Pageable pageable) {
        return bookRepository.findByActiveTrue(pageable);
    }

    @Override
    public Page<Book> findByCategory(Category category, Pageable pageable) {
        return bookRepository.findByCategoryAndActiveTrue(category, pageable);
    }

    @Override
    public Page<Book> searchBooks(String keyword, Pageable pageable) {
        return bookRepository.searchBooks(keyword, pageable);
    }

    @Override
    public Page<Book> searchBooksByCategory(String keyword, Category category, Pageable pageable) {
        return bookRepository.searchBooksByCategory(keyword, category, pageable);
    }

    @Override
    public List<Book> findLatestBooks() {
        return bookRepository.findTop8ByActiveTrueOrderByCreatedAtDesc();
    }

    @Override
    public List<Book> findTopDownloadedBooks() {
        return bookRepository.findTop8ByActiveTrueOrderByTotalDownloadsDesc();
    }

    @Override
    public long countBooks() {
        return bookRepository.countByActiveTrue();
    }

    @Override
    public Long sumTotalDownloads() {
        Long sum = bookRepository.sumTotalDownloads();
        return sum != null ? sum : 0L;
    }

    @Override
    @Transactional
    public void incrementDownloads(Long bookId) {
        Book book = findById(bookId);
        book.setTotalDownloads(book.getTotalDownloads() + 1);
        bookRepository.save(book);
    }
}