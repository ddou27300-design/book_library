package com.library.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.library.dto.ProfileDto;
import com.library.entity.User;
import com.library.service.DownloadHistoryService;
import com.library.service.FavoriteService;
import com.library.service.UserService;

import java.security.Principal;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final FavoriteService favoriteService;
    private final DownloadHistoryService downloadHistoryService;

    public UserController(UserService userService, FavoriteService favoriteService,
        DownloadHistoryService downloadHistoryService) {
        this.userService = userService;
        this.favoriteService = favoriteService;
        this.downloadHistoryService = downloadHistoryService;
    }

    private User getCurrentUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername());
    }

    @GetMapping("/profile")
    public String profile(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        model.addAttribute("user", user);
        model.addAttribute("profileDto", new ProfileDto());
        return "user/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute("profileDto") ProfileDto profileDto,
                                @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            userService.updateProfile(principal.getName(), profileDto, profileImage);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
        }
        return "redirect:/user/profile";
    }

    @GetMapping("/favorites")
    public String favorites(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        model.addAttribute("favorites", favoriteService.getUserFavorites(user));
        return "user/favorites";
    }

    @GetMapping("/downloads")
    public String downloadHistory(@RequestParam(defaultValue = "0") int page,
                                  Model model,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        model.addAttribute("history", downloadHistoryService.getUserDownloadHistory(user, PageRequest.of(page, 10)));
        model.addAttribute("currentPage", page);
        return "user/downloads";
    }
}
