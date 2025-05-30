package com.buildbetter.user;

import java.util.Map;
import java.util.UUID;

import com.buildbetter.user.model.User;

public interface UserAPI {
    boolean existsById(UUID userId);

    UUID getUserIdByEmail(String email);

    Map<UUID, User> getAllUsers(UUID requestingUserId);

    User getUserById(UUID userId);
}
