package com.library.service;

import com.library.dto.ProfileDto;
import com.library.dto.RegisterDto;
import com.library.entity.User;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    User register(RegisterDto dto);
    User findByEmail(String email);
    User findById(Long id);
    List<User> findAllUsers();
    List<User> searchUsers(String keyword);
    void updateProfile(String email, ProfileDto dto, MultipartFile file);
    void deleteUser(Long id);
    void toggleUserStatus(Long id);
    long countUsers();
    long countAdmins();
}