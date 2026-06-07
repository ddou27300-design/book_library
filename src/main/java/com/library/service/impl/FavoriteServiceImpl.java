package com.library.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.entity.Book;
import com.library.entity.Favorite;
import com.library.entity.User;
import com.library.repository.FavoriteRepository;
import com.library.service.FavoriteService;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;

    public FavoriteServiceImpl(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    @Override
    @Transactional
    public void toggleFavorite(User user, Book book) {
        if (favoriteRepository.existsByUserAndBook(user, book)) {
            favoriteRepository.deleteByUserAndBook(user, book);
        } else {
            // FIXED: Using standard Java object creation instead of Favorite.builder()
            Favorite favorite = new Favorite();
            favorite.setUser(user);
            favorite.setBook(book);
            
            favoriteRepository.save(favorite);
        }
    }

    @Override
    public boolean isFavorite(User user, Book book) {
        return favoriteRepository.existsByUserAndBook(user, book);
    }

    @Override
    public List<Favorite> getUserFavorites(User user) {
        return favoriteRepository.findByUser(user);
    }
}