package com.library.controller;

import com.library.entity.User;
import com.library.repository.ContactRepository;
import com.library.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/messages")
public class AdminContactController {

    private final ContactRepository contactRepository;
    private final UserService userService;

    public AdminContactController(ContactRepository contactRepository, UserService userService) {
        this.contactRepository = contactRepository;
        this.userService = userService;
    }

    private User getCurrentUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername());
    }

    @GetMapping
    public String viewMessages(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("currentUser", getCurrentUser(userDetails));
        model.addAttribute("messages", contactRepository.findAllByOrderByCreatedAtDesc());
        return "admin/messages";
    }
}
