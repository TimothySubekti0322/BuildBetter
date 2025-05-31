package com.buildbetter.article.dto;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddArticleRequest {
    private String author;

    @NotBlank(message = "Field 'title' is required")
    private String title;

    private MultipartFile banner;

    private String content;
}
