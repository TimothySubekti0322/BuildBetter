package com.buildbetter.user.dto.auth;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetUserResponse {
    private UUID id;
    private String phoneNumber;
    private String email;
    private String username;
    private String province;
    private String city;
    private String photo;
    private String role;
    private LocalDateTime createdAt;
}
