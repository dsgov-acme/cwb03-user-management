package io.nuvalence.user.management.api.service.service;

import io.nuvalence.auth.token.UserToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for MySelf.
 */
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    /**
     * Simple method to retrieve an authenticated userId of the security context.
     * @return an authenticated userId.
     */
    public Optional<String> getUserIdByAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<String> userId = Optional.empty();
        if (authentication instanceof UserToken userToken) {
            userId = Optional.ofNullable(userToken.getApplicationUserId());
        }
        return userId;
    }
}
