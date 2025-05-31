package com.buildbetter.article.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.buildbetter.article.model.Article;

public interface ArticleRepository extends JpaRepository<Article, UUID> {
    Optional<Article> findById(UUID id);
}
