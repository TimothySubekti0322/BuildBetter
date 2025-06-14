package com.buildbetter.article.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.buildbetter.article.dto.AddArticleRequest;
import com.buildbetter.article.dto.UpdateArticleRequest;
import com.buildbetter.article.model.Article;
import com.buildbetter.article.repository.ArticleRepository;
import com.buildbetter.shared.constant.S3Folder;
import com.buildbetter.shared.exception.BadRequestException;
import com.buildbetter.shared.util.S3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final S3Service s3Service;

    public UUID AddArticle(AddArticleRequest request) {
        log.info("Article Service : AddArticle");

        String folder = S3Folder.ARTICLES;

        log.info("Article Service : AddArticle - Upload File to S3");
        String bannerUrl = "";
        if (request.getBanner() != null && !request.getBanner().isEmpty()) {
            // Construct the folder path for the banner
            folder = S3Folder.ARTICLES + "/";
            // Upload the banner to S3 and get the URL
            log.info("Article Service : AddArticle - Upload Banner to S3");

            bannerUrl = s3Service.uploadFile(request.getBanner(), folder, "");
        }

        Article article = Article.builder()
                .author(request.getAuthor())
                .title(request.getTitle())
                .banner(bannerUrl)
                .content(request.getContent())
                .build();

        log.info("Article Service : AddArticle - Save article to DB");
        Article savedArticle = articleRepository.save(article);
        return savedArticle.getId();
    }

    public List<Article> GetAllArticles() {
        log.info("Article Service : GetAllArticles");
        return articleRepository.findAll();
    }

    public Article GetArticleById(UUID id) {
        log.info("Article Service : GetArticleById");
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Article not found with id: " + id));
        return article;
    }

    public void updateArticle(UpdateArticleRequest request, UUID id) {
        log.info("Article Service : UpdateArticle");
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Article not found with id: " + id));

        if (request.getAuthor() != null) {
            article.setAuthor(request.getAuthor());
        }
        if (request.getTitle() != null) {
            article.setTitle(request.getTitle());
        }
        if (request.getBanner() != null && !request.getBanner().isEmpty()) {
            String folder = S3Folder.ARTICLES + "/";
            log.info("Article Service : UpdateArticle - Upload Banner to S3");
            String bannerUrl = s3Service.uploadFile(request.getBanner(), folder, "");

            log.info("Article Service : UpdateArticle - Delete old banner from S3");
            s3Service.deleteFile(article.getBanner());

            article.setBanner(bannerUrl);
        }
        if (request.getContent() != null) {
            article.setContent(request.getContent());
        }
        log.info("Article Service : UpdateArticle - Save updated article to DB");
        articleRepository.save(article);
    }

    public void DeleteArticle(UUID id) {
        log.info("Article Service : DeleteArticle");
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Article not found with id: " + id));

        // Delete the banner from S3 if it exists
        if (article.getBanner() != null && !article.getBanner().isEmpty()) {
            log.info("Article Service : DeleteArticle - Delete banner from S3 : {}", article.getBanner());
            s3Service.deleteFile(article.getBanner());
        }

        log.info("Article Service : DeleteArticle - Delete article from DB");
        articleRepository.delete(article);

    }
}
