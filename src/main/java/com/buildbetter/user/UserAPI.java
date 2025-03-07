package com.buildbetter.user;

import java.util.UUID;

public interface UserAPI {
    boolean existsById(UUID userId);

    void verifiedUser(UUID userId);

    UUID getUserIdByEmail(String email);

    String getUserPassword(UUID userId);
}
