package com.library.controller;

import com.library.service.BookService;
import com.library.service.CategoryService;
import com.library.service.VisitorService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    private final BookService bookService;
    private final CategoryService categoryService;
    private final VisitorService visitorService;

    public HomeController(BookService bookService, CategoryService categoryService, VisitorService visitorService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
        this.visitorService = visitorService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String ua = request.getHeader("User-Agent");
        visitorService.logVisit(ip, ua, "/");

        model.addAttribute("latestBooks", bookService.findLatestBooks());
        model.addAttribute("topBooks", bookService.findTopDownloadedBooks());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("totalBooks", bookService.countBooks());
        model.addAttribute("totalDownloads", bookService.sumTotalDownloads());
        return "public/home";
    }

    @GetMapping("/about")
    public String about() {
        return "public/about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "public/contact";
    }

    @PostMapping("/contact")
    public String handleContact(@RequestParam String name,
                                @RequestParam String email,
                                @RequestParam String subject,
                                @RequestParam String message,
                                RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("success", "Your message has been sent successfully!");
        return "redirect:/contact";
    }
}
