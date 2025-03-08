package com.buildbetter.user;

import java.util.UUID;

public interface UserAPI {
    boolean existsById(UUID userId);

    UUID getUserIdByEmail(String email);
}
