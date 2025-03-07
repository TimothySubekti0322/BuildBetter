package com.buildbetter.auth;

public interface AuthAPI {
    void sendOtp(String userId, String email);
}
