package com.library.controller;

import com.library.entity.Book;
import com.library.entity.Category;
import com.library.entity.User;
import com.library.service.*;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final FavoriteService favoriteService;
    private final DownloadHistoryService downloadHistoryService;

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
        if (book.getPdfPath() == null) return "redirect:/books/" + id + "?error=nopdf";
        model.addAttribute("book", book);
        return "public/reader";
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> viewBook(@PathVariable Long id,
                                             @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(302).header(HttpHeaders.LOCATION, "/login").build();
        }

        Book book = bookService.findById(id);
        if (book.getPdfPath() == null) return ResponseEntity.notFound().build();

        try {
            Path filePath = Paths.get(book.getPdfPath()).toAbsolutePath();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) return ResponseEntity.notFound().build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadBook(@PathVariable Long id,
                                                  @AuthenticationPrincipal UserDetails currentUser,
                                                  HttpServletRequest request) {
        if (currentUser == null) {
            return ResponseEntity.status(302).header(HttpHeaders.LOCATION, "/login").build();
        }

        Book book = bookService.findById(id);
        if (book.getPdfPath() == null) return ResponseEntity.notFound().build();

        try {
            Path filePath = Paths.get(book.getPdfPath()).toAbsolutePath();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) return ResponseEntity.notFound().build();

            User user = userService.findByEmail(currentUser.getUsername());
            bookService.incrementDownloads(id);
            downloadHistoryService.recordDownload(user, book, request.getRemoteAddr());

            String filename = book.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
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
