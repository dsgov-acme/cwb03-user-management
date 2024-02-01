package io.nuvalence.user.management.api.service.controller;

import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.generated.controllers.MyselfApiDelegate;
import io.nuvalence.user.management.api.service.generated.models.UserDTO;
import io.nuvalence.user.management.api.service.generated.models.UserUpdateRequest;
import io.nuvalence.user.management.api.service.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Controller for cloud function mySelf actions.
 */
@Service
@RequiredArgsConstructor
class MySelfApiDelegateImpl implements MyselfApiDelegate {

    private static final String USER_NOT_FOUND_EXCEPTION_MESSAGE = "User not found!";

    private final UsersApiDelegateImpl usersApiDelegate;
    private final CurrentUserService currentUserService;

    @Override
    public ResponseEntity<UserDTO> getMySelf() {
        Optional<String> userId = currentUserService.getUserIdByAuthentication();
        return userId.map(id -> usersApiDelegate.getUserById(UUID.fromString(id)))
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_EXCEPTION_MESSAGE));
    }

    @Override
    public ResponseEntity<UserDTO> updateMySelf(UserUpdateRequest body) {
        Optional<String> userId = currentUserService.getUserIdByAuthentication();
        return userId.map(id -> usersApiDelegate.updateUserById(UUID.fromString(id), body))
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_EXCEPTION_MESSAGE));
    }
}
