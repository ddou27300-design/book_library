package com.library.service;

import com.library.entity.Category;

import java.util.List;

public interface CategoryService {
    Category save(Category category);
    Category findById(Long id);
    List<Category> findAll();
    void delete(Long id);
    boolean existsByName(String name);
}