package com.buildbetter.article.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buildbetter.article.dto.AddArticleRequest;
import com.buildbetter.article.dto.UpdateArticleRequest;
import com.buildbetter.article.model.Article;
import com.buildbetter.article.service.ArticleService;
import com.buildbetter.shared.dto.ApiResponseMessageAndData;
import com.buildbetter.shared.dto.ApiResponseMessageOnly;
import com.buildbetter.shared.dto.ApiResponseWithData;
import com.buildbetter.shared.security.annotation.IsAdmin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/articles")
@Slf4j
public class ArticleController {
    private final ArticleService articleService;

    @PostMapping(path = "", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @IsAdmin
    public ApiResponseMessageAndData<UUID> addArticle(@Valid @ModelAttribute AddArticleRequest request) {
        log.info("Article Controller : addArticle");

        UUID articleId = articleService.AddArticle(request);

        ApiResponseMessageAndData<UUID> response = new ApiResponseMessageAndData<>();
        response.setCode(HttpStatus.CREATED.value());
        response.setStatus(HttpStatus.CREATED.name());
        response.setMessage("Article added successfully");
        response.setData(articleId);

        return response;
    }

    @GetMapping("")
    public ApiResponseWithData<List<Article>> getAllArticles() {
        log.info("Article Controller : getAllArticles");

        List<Article> articles = articleService.GetAllArticles();

        ApiResponseWithData<List<Article>> response = new ApiResponseWithData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setData(articles);

        return response;
    }

    @GetMapping("/{id}")
    public ApiResponseWithData<Article> getArticleById(@PathVariable UUID id) {
        log.info("Article Controller : getArticleById");

        Article article = articleService.GetArticleById(id);

        ApiResponseWithData<Article> response = new ApiResponseWithData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setData(article);

        return response;
    }

    @PatchMapping(path = "/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @IsAdmin
    public ApiResponseMessageAndData<UUID> updateArticle(@PathVariable UUID id,
            @Valid @ModelAttribute UpdateArticleRequest request) {
        log.info("Article Controller : updateArticle");

        articleService.UpdateArticle(request, id);

        ApiResponseMessageAndData<UUID> response = new ApiResponseMessageAndData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("Article updated successfully");
        response.setData(id);
        return response;
    }

    @DeleteMapping("/{id}")
    @IsAdmin
    public ApiResponseMessageOnly deleteArticle(@PathVariable UUID id) {
        log.info("Article Controller : deleteArticle");

        articleService.DeleteArticle(id);
        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("Article deleted successfully");
        return response;
    }
}
