package com.turkcell.soccer.service;

import com.turkcell.soccer.model.Permission;
import com.turkcell.soccer.model.Role;
import com.turkcell.soccer.repository.PermissionRepository;
import com.turkcell.soccer.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private RoleService roleService;

    @Test
    void assignPermissions_whenRoleIsNull_shouldThrowException() {
        // Given
        String roleName = "ROLE_USER";
        String permissionName = "PERMISSION_USER";
        // When
        when(roleRepository.findByName(roleName)).thenReturn(Optional.empty());
        // Then
        assertThrowsExactly(NoSuchElementException.class, () -> roleService.assignPermissions(roleName, permissionName));
        verify(roleRepository, times(1)).findByName(roleName);
    }

    @Test
    void assignPermissions_whenPermissionIsNull_shouldThrowException() {
        // Given
        String roleName = "ROLE_USER";
        String permissionName = "PERMISSION_USER";
        Role role = new Role();

        // When
        when(roleRepository.findByName(roleName)).thenReturn(Optional.of(role));
        when(permissionRepository.findByName(permissionName)).thenReturn(Optional.empty());

        // Then
        assertThrowsExactly(NoSuchElementException.class, () -> roleService.assignPermissions(roleName, permissionName));
        verify(roleRepository, times(1)).findByName(roleName);
        verify(permissionRepository, times(1)).findByName(permissionName);

    }

    @Test
    void assignPermissions_whenRoleAndPermission_shouldReturn() {
        // Given
        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        String roleName = "ROLE_USER";
        String permissionName = "PERMISSION_USER";
        Role role = new Role();
        Set<Permission> permissions = new HashSet<>();
        role.setPermissions(permissions);
        Permission permission = new Permission();
        // When
        when(roleRepository.findByName(roleName)).thenReturn(Optional.of(role));
        when(permissionRepository.findByName(permissionName)).thenReturn(Optional.of(permission));
        when(roleRepository.save(role)).thenReturn(role);
        // Then
        roleService.assignPermissions(roleName, permissionName);

        verify(roleRepository, times(1)).findByName(roleName);
        verify(permissionRepository, times(1)).findByName(permissionName);
        verify(roleRepository, times(1)).save(roleCaptor.capture());


        Role savedRole = roleCaptor.getValue();
        assertTrue(savedRole.getPermissions().contains(permission));
        assertEquals(1, savedRole.getPermissions().size());
    }



}