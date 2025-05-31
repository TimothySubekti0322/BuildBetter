package com.buildbetter.article.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateArticleRequest {
    private String author;
    private String title;
    private MultipartFile banner;
    private String content;
}
