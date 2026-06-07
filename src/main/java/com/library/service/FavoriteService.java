package com.library.service;

import java.util.List;

import com.library.entity.Book;
import com.library.entity.Favorite;
import com.library.entity.User;

public interface FavoriteService {
    void toggleFavorite(User user, Book book);
    boolean isFavorite(User user, Book book);
    List<Favorite> getUserFavorites(User user);
}