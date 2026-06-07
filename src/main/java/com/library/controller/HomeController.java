package com.library.controller;

import com.library.entity.Contact;
import com.library.repository.ContactRepository;
import com.library.service.BookService;
import com.library.service.CategoryService;
import com.library.service.VisitorService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    private final BookService bookService;
    private final CategoryService categoryService;
    private final VisitorService visitorService;
    private final ContactRepository contactRepository;

    public HomeController(BookService bookService, CategoryService categoryService, VisitorService visitorService, ContactRepository contactRepository) {
        this.bookService = bookService;
        this.categoryService = categoryService;
        this.visitorService = visitorService;
        this.contactRepository = contactRepository;
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
    public String contact(Model model) {
        model.addAttribute("contact", new Contact());
        return "public/contact";
    }

    @PostMapping("/contact")
    public String handleContactSubmit(@ModelAttribute("contact") Contact contact,
                                      RedirectAttributes redirectAttributes) {
        try {
            contactRepository.save(contact);
            redirectAttributes.addFlashAttribute("success", "Your message has been sent successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to send message: " + e.getMessage());
        }
        return "redirect:/contact";
    }
}
