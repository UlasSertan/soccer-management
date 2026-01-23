package com.turkcell.soccer.service;

import com.turkcell.soccer.model.Permission;
import com.turkcell.soccer.model.Role;
import com.turkcell.soccer.repository.PermissionRepository;
import com.turkcell.soccer.repository.RoleRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Transactional
    public void assignPermissions(String roleName, String permissionName) {
        Role role = roleRepository.findByName(roleName).orElse(null);
        if (role == null) {
            log.warn("Role {} not found", roleName);
            throw new NoSuchElementException("Role " + roleName + " not found.");
        }

        Permission permission = permissionRepository.findByName(permissionName).orElse(null);
        if (permission == null) {
            log.warn("Permission {} not found", permissionName);
            throw new NoSuchElementException("Permission " + permissionName + " not found.");
        }

        role.getPermissions().add(permission);
        roleRepository.save(role);
        log.info("Role {} has been assigned to permission {}", roleName, permissionName);

    }


}
