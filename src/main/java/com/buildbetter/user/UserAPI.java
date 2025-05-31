package com.buildbetter.user;

import java.util.Map;
import java.util.UUID;

import com.buildbetter.user.dto.user.GetUserNameAndCity;

public interface UserAPI {
    boolean existsById(UUID userId);

    UUID getUserIdByEmail(String email);

    Map<UUID, GetUserNameAndCity> getAllUsersNameAndCity(UUID requestingUserId);

    GetUserNameAndCity getUserNameAndCityById(UUID userId);
}
