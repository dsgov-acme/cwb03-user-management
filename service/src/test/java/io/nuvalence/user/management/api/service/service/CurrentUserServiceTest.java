package io.nuvalence.user.management.api.service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import io.nuvalence.auth.token.UserToken;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @InjectMocks private CurrentUserService currentUserService;

    private UserEntity createUserEntity() {
        UserEntity userEntity = new UserEntity();
        userEntity.setFirstName("John");
        userEntity.setMiddleName("Locke");
        userEntity.setLastName("Doe");
        userEntity.setEmail("Skipper@theIsland.com");
        userEntity.setPhoneNumber("555-555-5555");
        userEntity.setExternalId("TestExternalId1234");
        userEntity.setId(UUID.fromString("ca8cfd1b-8643-4185-ba7f-8c8fbc9a7da6"));
        userEntity.setRoles(new ArrayList<>());
        return userEntity;
    }

    @Test
    void testGetUserIdByAuthentication() {

        Optional<UserEntity> userEntity = Optional.of(createUserEntity());

        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(securityContext.getAuthentication())
                .thenReturn(
                        new UserToken(
                                "EXT000123",
                                userEntity.get().getId().toString(),
                                "",
                                "",
                                "",
                                Collections.emptyList(),
                                "",
                                ""));
        Optional<String> userid = currentUserService.getUserIdByAuthentication();
        userid.ifPresent(s -> assertEquals(userEntity.get().getId().toString(), s));
    }

    @Test
    void notUserFoundInAuthToken() {
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(securityContext.getAuthentication())
                .thenReturn(
                        new UserToken(
                                "EXT000123", null, "", "", "", Collections.emptyList(), "", ""));
        Optional<String> userid = currentUserService.getUserIdByAuthentication();
        assertTrue(userid.isEmpty());
    }
}
