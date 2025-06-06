package com.buildbetter.user.api;

import java.util.Map;
import java.util.UUID;

public interface UserAPI {
    boolean existsById(UUID userId);

    UUID getUserIdByEmail(String email);

    Map<UUID, GetUserNameAndCity> getAllUsersNameAndCity(UUID requestingUserId);

    GetUserNameAndCity getUserNameAndCityById(UUID userId);
}
