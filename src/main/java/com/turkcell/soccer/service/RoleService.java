package com.turkcell.soccer.service;

import com.turkcell.soccer.model.Permission;
import com.turkcell.soccer.model.Role;
import com.turkcell.soccer.repository.PermissionRepository;
import com.turkcell.soccer.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }


    public void assignPermissions(String roleName, String permissionName) {
        Role role = roleRepository.findByName(roleName).orElseThrow(
                () -> new BadCredentialsException("Role " + roleName + " not found.")
        );
        Permission permission = permissionRepository.findByName(permissionName).orElseThrow(
                () -> new BadCredentialsException("Permission " + permissionName + " not found.")
        );

        role.getPermissions().add(permission);
        roleRepository.save(role);

    }


}
