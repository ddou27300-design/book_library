package com.library.controller;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.library.entity.Book;
import com.library.entity.Category;
import com.library.entity.User;
import com.library.service.BookService;
import com.library.service.CategoryService;
import com.library.service.DownloadHistoryService;
import com.library.service.FavoriteService;
import com.library.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/books")
public class BookController {

    private static final Logger log = LoggerFactory.getLogger(BookController.class);

    private final BookService bookService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final FavoriteService favoriteService;
    private final DownloadHistoryService downloadHistoryService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public BookController(BookService bookService, CategoryService categoryService,
                          UserService userService, FavoriteService favoriteService,
                          DownloadHistoryService downloadHistoryService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
        this.userService = userService;
        this.favoriteService = favoriteService;
        this.downloadHistoryService = downloadHistoryService;
    }

    @GetMapping
    public String catalog(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model,
            @AuthenticationPrincipal UserDetails currentUser) {

        Page<Book> bookPage;
        Category selectedCategory = null;

        if (categoryId != null) {
            selectedCategory = categoryService.findById(categoryId);
            if (keyword != null && !keyword.isBlank()) {
                bookPage = bookService.searchBooksByCategory(keyword, selectedCategory, PageRequest.of(page, size));
            } else {
                bookPage = bookService.findByCategory(selectedCategory, PageRequest.of(page, size));
            }
        } else if (keyword != null && !keyword.isBlank()) {
            bookPage = bookService.searchBooks(keyword, PageRequest.of(page, size));
        } else {
            bookPage = bookService.findAllActive(PageRequest.of(page, size));
        }

        model.addAttribute("books", bookPage);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("selectedCategory", selectedCategory);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookPage.getTotalPages());
        return "public/catalog";
    }

    @GetMapping("/{id}")
    public String bookDetail(@PathVariable Long id, Model model,
                             @AuthenticationPrincipal UserDetails currentUser) {
        Book book = bookService.findById(id);
        model.addAttribute("book", book);

        if (currentUser != null) {
            User user = userService.findByEmail(currentUser.getUsername());
            model.addAttribute("isFavorite", favoriteService.isFavorite(user, book));
            model.addAttribute("currentUser", user);
        }
        return "public/book-detail";
    }

    @GetMapping("/{id}/read")
    public String readBook(@PathVariable Long id, Model model,
                           @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) return "redirect:/login";
        Book book = bookService.findById(id);
        if (book.getPdfFileUrl() == null) return "redirect:/books/" + id + "?error=nopdf";
        model.addAttribute("book", book);
        return "public/reader";
    }

    @GetMapping("/{id}/view")
    public void viewBook(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails currentUser,
                         jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        if (currentUser == null) {
            response.sendRedirect("/login");
            return;
        }

        Book book = bookService.findById(id);
        if (book == null || book.getPdfFileUrl() == null) {
            response.sendError(404);
            return;
        }

        String pdfUrl = book.getPdfFileUrl();

        try {
            if (isCloudUrl(pdfUrl)) {
                streamCloudPdf(pdfUrl, response);
            } else {
                streamLocalPdf(pdfUrl, response);
            }
        } catch (Exception e) {
            log.error("viewBook: failed to stream PDF for book id={}: {}", id, e.getMessage());
            response.sendError(502);
        }
    }

    @GetMapping({"/{id}/download-file", "/{id}/download"})
    public ResponseEntity<Resource> downloadBook(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest request) {

        if (currentUser == null) {
            log.warn("Download blocked: unauthenticated user for book id={}", id);
            return ResponseEntity.status(302)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        Book book;
        try {
            book = bookService.findById(id);
        } catch (Exception e) {
            log.error("Download failed: book id={} not found: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }

        if (book == null) {
            log.warn("Download failed: book id={} is null", id);
            return ResponseEntity.notFound().build();
        }

        String pdfUrl = book.getPdfFileUrl();

        if (pdfUrl == null || pdfUrl.isBlank()) {
            log.warn("Download failed: book id='{}' title='{}' has no pdfFileUrl", id, book.getTitle());
            return ResponseEntity.notFound().build();
        }

        log.info("Download requested: book id={}, title='{}', pdfUrl='{}'", id, book.getTitle(), pdfUrl);

        try {
            Resource resource;
            String filename = buildFilename(book.getTitle());

            if (isCloudUrl(pdfUrl)) {
                resource = downloadFromCloud(pdfUrl, id);
            } else {
                resource = downloadFromLocal(pdfUrl, id);
            }

            User user = userService.findByEmail(currentUser.getUsername());
            bookService.incrementDownloads(id);
            downloadHistoryService.recordDownload(user, book, request.getRemoteAddr());

            String encodedFilename = java.net.URLEncoder.encode(filename, StandardCharsets.UTF_8)
                    .replace("+", "%20");

            String contentDisposition = "attachment; "
                    + "filename=\"" + filename + "\"; "
                    + "filename*=UTF-8''" + encodedFilename;

            log.info("Download OK: book id={}, size={} bytes", id, resource.contentLength());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(resource.contentLength())
                    .body(resource);

        } catch (Exception e) {
            log.error("Download FAILED: book id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // =====================================================================
    //  PRIVATE HELPER METHODS
    // =====================================================================

    private boolean isCloudUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    private Resource downloadFromCloud(String cloudUrl, Long bookId) throws Exception {
        log.info("Fetching cloud PDF for book id={}: {}", bookId, cloudUrl);

        java.net.URL url = new java.net.URL(cloudUrl);
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);
        conn.setInstanceFollowRedirects(true);
        conn.setRequestMethod("GET");

        int httpStatus = conn.getResponseCode();
        if (httpStatus != 200) {
            conn.disconnect();
            throw new RuntimeException("Cloud storage returned HTTP " + httpStatus + " for book id=" + bookId);
        }

        byte[] pdfBytes = conn.getInputStream().readAllBytes();
        conn.disconnect();

        log.info("Cloud download OK: {} bytes for book id={}", pdfBytes.length, bookId);
        return new org.springframework.core.io.ByteArrayResource(pdfBytes);
    }

    /**
     * FIX: Resolves the local PDF path correctly.
     * FileStorageServiceImpl saves paths as "/uploads/books/uuid.pdf".
     * We strip the leading slash and resolve from the project root (not from uploadDir),
     * which avoids the "uploads/uploads/..." doubled-path bug.
     */
    private Resource downloadFromLocal(String localPath, Long bookId) throws Exception {
        // Strip leading slash → "uploads/books/uuid.pdf"
        String cleanPath = localPath.startsWith("/") ? localPath.substring(1) : localPath;

        // Resolve from project root, NOT from uploadDir (avoids uploads/uploads doubling)
        Path filePath = Paths.get(cleanPath).toAbsolutePath().normalize();

        log.info("Resolving local PDF: {}", filePath);

        if (!Files.exists(filePath)) {
            throw new RuntimeException("Local PDF file not found: " + filePath);
        }

        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) {
            throw new RuntimeException("Cannot read local PDF: " + filePath);
        }

        log.info("Local PDF OK: {} bytes for book id={}", resource.contentLength(), bookId);
        return resource;
    }

    private String buildFilename(String title) {
        if (title == null || title.isBlank()) {
            return "download.pdf";
        }
        String safe = title.replaceAll("[^\\p{L}\\p{N}\\s._-]", "_").trim();
        return safe.isEmpty() ? "download.pdf" : safe + ".pdf";
    }

    private void streamCloudPdf(String cloudUrl, jakarta.servlet.http.HttpServletResponse response) throws Exception {
        java.net.URL url = new java.net.URL(cloudUrl);
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);
        conn.setInstanceFollowRedirects(true);

        if (conn.getResponseCode() != 200) {
            conn.disconnect();
            response.sendError(502);
            return;
        }

        byte[] pdfBytes = conn.getInputStream().readAllBytes();
        conn.disconnect();

        response.setContentType("application/pdf");
        response.setContentLength(pdfBytes.length);
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }

    /**
     * FIX: Same path fix as downloadFromLocal — resolve from project root.
     */
    private void streamLocalPdf(String localPath, jakarta.servlet.http.HttpServletResponse response) throws Exception {
        // Strip leading slash → "uploads/books/uuid.pdf"
        String cleanPath = localPath.startsWith("/") ? localPath.substring(1) : localPath;

        // Resolve from project root, NOT from uploadDir
        Path filePath = Paths.get(cleanPath).toAbsolutePath().normalize();

        if (!Files.exists(filePath)) {
            log.error("streamLocalPdf: file not found at {}", filePath);
            response.sendError(404);
            return;
        }

        response.setContentType("application/pdf");
        response.setContentLengthLong(Files.size(filePath));

        try (java.io.InputStream is = Files.newInputStream(filePath)) {
            is.transferTo(response.getOutputStream());
            response.getOutputStream().flush();
        }
    }

    @PostMapping("/{id}/favorite")
    public String toggleFavorite(@PathVariable Long id,
        @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) return "redirect:/login";
        User user = userService.findByEmail(currentUser.getUsername());
        Book book = bookService.findById(id);
        favoriteService.toggleFavorite(user, book);
        return "redirect:/books/" + id;
    }
}