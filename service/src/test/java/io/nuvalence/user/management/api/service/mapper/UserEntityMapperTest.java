package io.nuvalence.user.management.api.service.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.enums.UserType;
import io.nuvalence.user.management.api.service.generated.models.UserCreationRequest;
import io.nuvalence.user.management.api.service.generated.models.UserDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class UserEntityMapperTest {

    @Test
    void testMapperInstance_shouldNotBeNull() {
        assertNotNull(UserEntityMapper.INSTANCE);
    }

    @Test
    void shouldMapUserEntityToUserDto() {
        UserEntity user = new UserEntity();
        user.setFirstName("John");
        user.setMiddleName("Locke");
        user.setLastName("Doe");
        user.setEmail("Invisable@google.com");
        user.setExternalId("48QI42I8CWObQuCvk2uuF3XlyS63");
        user.setPhoneNumber("555-555-5555");

        UserDTO userModel = UserEntityMapper.INSTANCE.convertUserEntityToUserModel(user);
        assertEquals(user.getFirstName(), userModel.getFirstName());
        assertEquals(user.getMiddleName(), userModel.getMiddleName());
        assertEquals(user.getLastName(), userModel.getLastName());
        assertEquals(user.getPhoneNumber(), userModel.getPhoneNumber());
        assertEquals(user.getEmail(), userModel.getEmail());
        assertEquals(user.getExternalId(), userModel.getExternalId());
    }

    @Test
    void shouldMapUserDtoToUserEntity() {
        UserDTO user = new UserDTO();
        user.setFirstName("John");
        user.setMiddleName("Locke");
        user.setLastName("Doe");
        user.setEmail("Invisable@google.com");
        user.setExternalId("48QI42I8CWObQuCvk2uuF3XlyS63");
        user.setPhoneNumber("555-555-5555");

        UserEntity userEntity = UserEntityMapper.INSTANCE.convertUserModelToUserEntity(user);
        assertEquals(user.getFirstName(), userEntity.getFirstName());
        assertEquals(user.getMiddleName(), userEntity.getMiddleName());
        assertEquals(user.getLastName(), userEntity.getLastName());
        assertEquals(user.getPhoneNumber(), userEntity.getPhoneNumber());
        assertEquals(user.getEmail(), userEntity.getEmail());
        assertEquals(user.getExternalId(), userEntity.getExternalId());
    }

    @Test
    void shouldMapUserEntityToUserDtoWithRolesPresent() {
        UserEntity user = new UserEntity();
        user.setFirstName("John");
        user.setMiddleName("Locke");
        user.setLastName("Doe");
        user.setEmail("Invisable@google.com");
        user.setExternalId("48QI42I8CWObQuCvk2uuF3XlyS63");
        user.setPhoneNumber("555-555-5555");

        RoleEntity role = new RoleEntity();
        role.setId(UUID.randomUUID());
        role.setName("Role test");
        role.setDescription("Role description");
        role.setUsers(List.of(user));
        role.setPermissions(List.of());

        user.setRoles(List.of(role));

        UserDTO userModel = UserEntityMapper.INSTANCE.convertUserEntityToUserModel(user);
        assertEquals(user.getFirstName(), userModel.getFirstName());
        assertEquals(user.getMiddleName(), userModel.getMiddleName());
        assertEquals(user.getLastName(), userModel.getLastName());
        assertEquals(user.getPhoneNumber(), userModel.getPhoneNumber());
        assertEquals(user.getEmail(), userModel.getEmail());
        assertEquals(user.getExternalId(), userModel.getExternalId());

        assertEquals(1, user.getRoles().size());
        assertEquals(role.getId(), user.getRoles().get(0).getId());
        assertEquals(role.getName(), user.getRoles().get(0).getName());
        assertEquals(role.getDescription(), user.getRoles().get(0).getDescription());
    }

    @Test
    void shouldConvertUserCreationRequestToUserEntity() {

        UserCreationRequest userCreationRequest =
                new UserCreationRequest("a@b.io", "ABCD", "idgiver", "public");

        UserEntity userEntity =
                UserEntityMapper.INSTANCE.convertUserCreationRequestToUserEntity(
                        userCreationRequest);

        assertEquals("a@b.io", userEntity.getEmail());
        assertEquals("ABCD", userEntity.getExternalId());
        assertEquals("idgiver", userEntity.getIdentityProvider());
        assertEquals(UserType.PUBLIC, userEntity.getUserType());
        assertEquals(false, userEntity.getDeleted());
    }
}
