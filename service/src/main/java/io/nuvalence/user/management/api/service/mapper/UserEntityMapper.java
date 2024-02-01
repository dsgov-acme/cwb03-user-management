package io.nuvalence.user.management.api.service.mapper;

import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.enums.UserType;
import io.nuvalence.user.management.api.service.generated.models.UserCreationRequest;
import io.nuvalence.user.management.api.service.generated.models.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Objects;

/**
 * Maps transaction definitions between UserEntity and User.
 */
@Mapper(componentModel = "spring")
public interface UserEntityMapper extends LazyLoadingAwareMapper {
    UserEntityMapper INSTANCE = Mappers.getMapper(UserEntityMapper.class);

    /**
     * Maps {@link UserEntity} to
     * {@link UserDTO}.
     *
     * @param userEntity an entity
     * @return user model
     */
    @Mapping(target = "preferences", ignore = true)
    @Mapping(target = "assignedRoles", ignore = true)
    @Mapping(target = "displayName", expression = "java(createDisplayname(userEntity))")
    UserDTO convertUserEntityToUserModel(UserEntity userEntity);

    /**
     * Maps {@link UserDTO} to
     * {@link UserEntity}.
     *
     * @param user is a model
     * @return an entity.
     */
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userPreference", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    UserEntity convertUserModelToUserEntity(UserDTO user);

    /**
     * Maps {@link UserCreationRequest} to
     * {@link UserEntity}.
     *
     * @param user is user creation request DTO
     * @return a user entity.
     */
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userPreference", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "deleted", expression = "java(false)")
    UserEntity convertUserCreationRequestToUserEntity(UserCreationRequest user);

    /**
     * Converts a string to a UserType.
     *
     * @param userType user type string
     * @return UserType enum instance.
     */
    default UserType convertStringToUserType(String userType) {
        return UserType.fromText(userType);
    }

    /**
     * Convert a UserType to a string.
     *
     * @param userType UserType enum
     * @return string value for user type
     */
    default String convertUserToTypeString(UserType userType) {
        if (userType == null) {
            return null;
        }
        return userType.toString();
    }

    /**
     * create display name for user.
     *
     * @param user user Entity
     * @return string value for the display name
     */
    default String createDisplayname(UserEntity user) {
        return Objects.toString(user.getFirstName(), "")
                .concat(" ")
                .concat(Objects.toString(user.getLastName(), ""))
                .trim();
    }
}
