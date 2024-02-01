package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.config.exception.BusinessLogicException;
import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.generated.models.UserUpdateRequest;
import io.nuvalence.user.management.api.service.repository.RoleRepository;
import io.nuvalence.user.management.api.service.repository.UserRepository;
import io.nuvalence.user.management.api.service.util.ExceptionInspectionUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for User.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("checkstyle:ClassFanOutComplexity")
public class UserService {
    private final Clock clock = Clock.systemDefaultZone();
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserPreferenceService userPreferenceService;
    private final RoleService roleService;

    /**
     * Creates a User Entity from a user model.
     *
     * @param user represents a user model
     * @return the new user entity.
     */
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    public UserEntity createUser(UserEntity user) {
        return userRepository.save(user);
    }

    /**
     * Updates a User Entity from a (partial) user model.
     *
     * @param userId is a user's userId
     * @param updateRequest is a user update request with changes to be made
     * @return the updated UserEntity object
     *
     * @throws ResourceNotFoundException if the user is not found
     * @throws BusinessLogicException if the user's email is already in use
     */
    public UserEntity updateUserById(UUID userId, UserUpdateRequest updateRequest) {
        // Validate that userId has been provided and corresponds to an actual user
        if (userId == null) {
            throw new ResourceNotFoundException("Missing user Id.");
        }

        UserEntity userEntity =
                userRepository
                        .findByIdLoaded(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        // Update email if provided
        updateNames(userEntity, updateRequest);
        updateEmail(userEntity, updateRequest);
        userEntity.setPhoneNumber(updateRequest.getPhoneNumber());

        return userRepository.save(userEntity);
    }

    /**
     * Deletes a user entity from the DB.
     * @param userId is a user's id
     */
    public void deleteUser(UUID userId) {
        final OffsetDateTime now = OffsetDateTime.now(clock);
        UserEntity userEntity = getUserEntityById(userId);
        userEntity.setDeleted(true);
        userEntity.setDeletedOn(now);
        userRepository.save(userEntity);
    }

    /**
     * Assigns a role to a particular user using its ID.
     *
     * @param userId is the user's ID.
     * @param roleId is the role's ID.
     */
    public void assignRoleToUser(UUID userId, UUID roleId) {
        UserEntity userEntity = getUserEntityById(userId);
        RoleEntity roleEntity = getRoleEntityById(roleId);

        userEntity.getRoles().add(roleEntity);
        userRepository.save(userEntity);
    }

    /**
     * Deletes role for a particular user.
     *
     * @param userId is the user's ID.
     * @param roleId is the role's ID.
     *
     * @throws BusinessLogicException if the role does not exist
     */
    public void removeRoleFromUser(UUID userId, UUID roleId) {
        // Safely handle a missing User ID or Role ID.
        UserEntity userEntity = getUserEntityById(userId);
        getRoleEntityById(roleId);

        if (userEntity.getRoles() == null) {
            userEntity.setRoles(new ArrayList<>());
        }

        int initialLength = userEntity.getRoles().size();

        // Only bother removing if there is something to remove.
        if (initialLength > 0) {
            userEntity.setRoles(
                    userEntity.getRoles().stream().filter(role -> role.getId() != roleId).toList());
        }

        int updatedLength = userEntity.getRoles().size();

        // Either the roles did not change, or we're removing a role from an empty list.
        if (initialLength == updatedLength) {
            throw new BusinessLogicException(
                    String.format("The role requested does not exist: %s.", userEntity.getId()));
        }

        userRepository.save(userEntity);
    }

    /**
     * Gets a list of all users.
     *
     * @param pageable the pagination information (page number, size, and sort)
     * @return a list of UserDTOs
     */
    public Page<UserEntity> getUserList(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * Returns a UserDTO based on the user's id.
     *
     * @param userId is the user's id.
     * @return a UserEntity.
     */
    public Optional<UserEntity> getUserById(UUID userId) {
        return userRepository.findById(userId);
    }

    public Optional<UserEntity> getUserByIdLoaded(UUID userId) {
        return userRepository.findByIdLoaded(userId);
    }

    public Page<UserEntity> getUsersBySearchCriteria(
            UserSearchCriteria searchCriteria, Pageable pageable) {
        return userRepository.findAll(searchCriteria, pageable);
    }

    /**
     * Inspects exception for root cause to see if this was a violation of the database constraint preventing duplicate
     * external account IDs.
     *
     * @param e Exception to inspect
     * @return True if root cause was violation of the database constraint preventing duplicate external account IDs
     */
    public boolean isDuplicateExternalUserException(final RuntimeException e) {
        return ExceptionInspectionUtility.findCauseOfType(e, ConstraintViolationException.class)
                .map(ConstraintViolationException.class::cast)
                .map(
                        cve ->
                                "user_table_identity_provider_external_id_deleted_deleted_on_key"
                                        .equals(cve.getConstraintName()))
                .orElse(false);
    }

    private void updateNames(UserEntity userEntity, UserUpdateRequest updateRequest) {
        userEntity.setFirstName(updateRequest.getFirstName());
        userEntity.setMiddleName(updateRequest.getMiddleName());
        userEntity.setLastName(updateRequest.getLastName());
    }

    private void updateEmail(UserEntity userEntity, UserUpdateRequest updateRequest) {
        if (updateRequest.getEmail() != null) {
            userEntity.setEmail(updateRequest.getEmail());
        }
    }

    private RoleEntity getRoleEntityById(UUID roleId) {
        return roleRepository
                .findById(roleId)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Role with ID '" + roleId + "' not found."));
    }

    private UserEntity getUserEntityById(UUID userId) {
        return this.getUserById(userId)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "User with ID '" + userId + "' not found."));
    }

    /**
     * Trims a user entity to only include public fields.
     * @param user the entity to trim
     * @return trimmed user
     */
    public UserEntity trimUserDataToPublicFields(UserEntity user) {

        UserEntity trimmedUser = new UserEntity();
        trimmedUser.setId(user.getId());
        trimmedUser.setFirstName(user.getFirstName());
        trimmedUser.setLastName(user.getLastName());
        trimmedUser.setEmail(user.getEmail());

        return trimmedUser;
    }

    public void addRoleToUser(UserEntity user, RoleEntity role) {
        user.getRoles().add(role);
        userRepository.save(user);
    }

    public void deleteRoleFromUser(UserEntity user, RoleEntity role) {
        user.getRoles().remove(role);
        userRepository.save(user);
    }
}
