package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.config.exception.BusinessLogicException;
import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.PermissionEntity;
import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.generated.models.RoleUpsertRequest;
import io.nuvalence.user.management.api.service.repository.ApplicationRepository;
import io.nuvalence.user.management.api.service.repository.PermissionRepository;
import io.nuvalence.user.management.api.service.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for Roles.
 */
@Component
@Transactional
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings({"checkstyle:ClassFanOutComplexity"})
public class RoleService {

    private final RoleRepository roleRepository;
    private final ApplicationRepository applicationRepository;
    private final PermissionRepository permissionRepository;

    /**
     * Gets a role, if it exists.
     * @param id Role ID.
     * @return role with ID.
     */
    public Optional<RoleEntity> getRole(UUID id) {
        return roleRepository.findById(id);
    }

    /**
     * Gets a role, if it exists.
     * @param name Role name.
     * @return role with name.
     */
    public Optional<RoleEntity> getRoleByName(String name) {
        return roleRepository.findByName(name);
    }

    /**
     * Inserts or updates a Role based on if an ID is provided.
     * @param request Data to create / update the role from.
     * @return updated Role.
     */
    public RoleEntity upsertRole(RoleUpsertRequest request) {
        if (request.getId() != null) {
            return this.updateRole(request.getId(), request);
        }

        return this.addRole(request);
    }

    /**
     * Adds a role to the database.
     *
     * @param request the role creation request object
     * @return the new role entity.
     *
     * @throws BusinessLogicException if the role already exists.
     */
    public RoleEntity addRole(RoleUpsertRequest request) {
        Optional<RoleEntity> nameCheck =
                roleRepository.findByName(request.getName().toUpperCase(Locale.US));

        if (nameCheck.isPresent()) {
            throw new BusinessLogicException("This role already exists.");
        }

        RoleEntity newRole = new RoleEntity();

        // Ensures uppercase on submission
        newRole.setName(request.getName().toUpperCase(Locale.US));

        newRole.setUsers(Collections.emptyList());
        newRole.setDescription(request.getDescription());

        // Validate permissions
        newRole.setPermissions(validatePermissions(request.getPermissions()));

        return roleRepository.save(newRole);
    }

    /**
     * Updates a role.
     * @param roleId The id of the role
     * @param request The role update request object
     * @return the updated role entity.
     */
    public RoleEntity updateRole(UUID roleId, RoleUpsertRequest request) {
        RoleEntity role =
                roleRepository
                        .findById(roleId)
                        .orElseThrow(() -> new BusinessLogicException("This role does not exist."));

        role.setName(request.getName().toUpperCase(Locale.US));
        role.setDescription(request.getDescription());

        // Validate the permissions
        role.setPermissions(validatePermissions(request.getPermissions()));

        return roleRepository.save(role);
    }

    /**
     * Fetches a list of all roles that exist.
     * @return a list of all the roles.
     */
    public List<RoleEntity> getAllRoles() {
        return roleRepository.findAll();
    }

    /**
     * Deletes a role by its id.
     *
     * @param roleId is an id of a role.
     * @param resourceName The name of the resource.
     */
    public void deleteRoleById(UUID roleId, String resourceName) {
        RoleEntity roleEntity =
                roleRepository
                        .findById(roleId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "There is no role that exists with this id."));

        roleRepository.delete(roleEntity);
    }

    private List<PermissionEntity> validatePermissions(List<String> expectedPermissions) {
        List<PermissionEntity> permissions =
                permissionRepository.findAllByApplicationRole(expectedPermissions);

        Set<String> fetchedPermissions =
                permissions.stream()
                        .map(PermissionEntity::getApplicationRole)
                        .collect(Collectors.toUnmodifiableSet());

        for (String permission : expectedPermissions) {
            if (!fetchedPermissions.contains(permission)) {
                throw new BusinessLogicException(
                        "The provided permission '" + permission + "' is invalid.");
            }
        }

        return permissions;
    }
}
