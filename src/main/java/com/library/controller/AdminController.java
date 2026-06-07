package com.library.controller;

import com.library.dto.BookDto;
import com.library.entity.Book;
import com.library.entity.Category;
import com.library.entity.User;
import com.library.service.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final BookService bookService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final DownloadHistoryService downloadHistoryService;
    private final VisitorService visitorService;
    private final FileStorageService fileStorageService;

    public AdminController(BookService bookService, CategoryService categoryService,
                           UserService userService, DownloadHistoryService downloadHistoryService,
                           VisitorService visitorService, FileStorageService fileStorageService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
        this.userService = userService;
        this.downloadHistoryService = downloadHistoryService;
        this.visitorService = visitorService;
        this.fileStorageService = fileStorageService;
    }

    private User getCurrentUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername());
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("currentUser", getCurrentUser(userDetails));
        model.addAttribute("totalBooks", bookService.countBooks());
        model.addAttribute("totalUsers", userService.countUsers());
        model.addAttribute("totalDownloads", bookService.sumTotalDownloads());
        model.addAttribute("todayDownloads", downloadHistoryService.countTodayDownloads());
        model.addAttribute("todayVisitors", visitorService.countTodayVisitors());
        model.addAttribute("totalVisitors", visitorService.countTotalVisitors());
        model.addAttribute("latestBooks", bookService.findLatestBooks());
        model.addAttribute("recentDownloads", downloadHistoryService.getRecentDownloads());
        model.addAttribute("downloadsPerDay", downloadHistoryService.getDownloadsPerDay(7));
        model.addAttribute("visitorsPerDay", visitorService.getVisitorsPerDay(7));
        return "admin/dashboard";
    }

    @GetMapping("/books")
    public String books(@RequestParam(required = false) String keyword,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("currentUser", getCurrentUser(userDetails));

        var bookPage = (keyword != null && !keyword.isBlank())
                ? bookService.searchBooks(keyword, PageRequest.of(page, size))
                : bookService.findAllActive(PageRequest.of(page, size));

        model.addAttribute("books", bookPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookPage.getTotalPages());
        return "admin/books";
    }

    @GetMapping("/books/add")
    public String addBookForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("currentUser", getCurrentUser(userDetails));
        model.addAttribute("bookDto", new BookDto());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("isEdit", false);
        return "admin/book-form";
    }

    @PostMapping("/books/add")
    public String addBook(@Valid @ModelAttribute("bookDto") BookDto dto,
                          BindingResult result,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("isEdit", false);
            return "admin/book-form";
        }

        try {
            bookService.addBook(dto);
            redirectAttributes.addFlashAttribute("success", "Book added successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/books";
    }

    @GetMapping("/books/{id}/edit")
    public String editBookForm(@PathVariable Long id, Model model,
                               @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("currentUser", getCurrentUser(userDetails));
        Book book = bookService.findById(id);

        BookDto dto = new BookDto();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setDescription(book.getDescription());
        dto.setCategoryId(book.getCategory().getId());
        dto.setIsbn(book.getIsbn());
        dto.setPublishYear(book.getPublishYear());
        dto.setPublisher(book.getPublisher());
        dto.setLanguage(book.getLanguage());
        dto.setExistingPdfPath(book.getPdfPath());
        dto.setExistingCoverImage(book.getCoverImage());

        model.addAttribute("bookDto", dto);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("isEdit", true);
        return "admin/book-form";
    }

    @PostMapping("/books/{id}/edit")
    public String updateBook(@PathVariable Long id,
                             @Valid @ModelAttribute("bookDto") BookDto dto,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("isEdit", true);
            return "admin/book-form";
        }

        try {
            bookService.updateBook(id, dto);
            redirectAttributes.addFlashAttribute("success", "Book updated successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/books";
    }

    @PostMapping("/books/{id}/delete")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteBook(id);
            redirectAttributes.addFlashAttribute("success", "Book deleted successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/books";
    }

    @GetMapping("/categories")
    public String categories(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("currentUser", getCurrentUser(userDetails));
        model.addAttribute("categories", categoryService.findAll());
        return "admin/categories";
    }

    @PostMapping("/categories")
    public String addCategory(@RequestParam String name,
                              @RequestParam(required = false) String description,
                              RedirectAttributes redirectAttributes) {
        try {
            if (categoryService.existsByName(name)) {
                redirectAttributes.addFlashAttribute("error", "Category '" + name + "' already exists");
                return "redirect:/admin/categories";
            }
            Category category = new Category();
            category.setName(name);
            category.setDescription(description);
            categoryService.save(category);
            redirectAttributes.addFlashAttribute("success", "Category added successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Category deleted successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/users")
    public String users(@RequestParam(required = false) String keyword,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("currentUser", getCurrentUser(userDetails));

        var users = (keyword != null && !keyword.isBlank())
                ? userService.searchUsers(keyword)
                : userService.findAllUsers();

        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword);
        return "admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("success", "User status updated successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/statistics")
    public String statistics(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("currentUser", getCurrentUser(userDetails));
        model.addAttribute("totalBooks", bookService.countBooks());
        model.addAttribute("totalUsers", userService.countUsers());
        model.addAttribute("totalDownloads", bookService.sumTotalDownloads());
        model.addAttribute("todayDownloads", downloadHistoryService.countTodayDownloads());
        model.addAttribute("todayVisitors", visitorService.countTodayVisitors());
        model.addAttribute("totalVisitors", visitorService.countTotalVisitors());
        model.addAttribute("downloadsPerDay", downloadHistoryService.getDownloadsPerDay(30));
        model.addAttribute("visitorsPerDay", visitorService.getVisitorsPerDay(30));
        model.addAttribute("topBooks", bookService.findTopDownloadedBooks());
        return "admin/statistics";
    }
}
