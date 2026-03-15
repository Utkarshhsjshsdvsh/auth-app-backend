package com.substring.auth.auth_app_backend.Repositories;

import com.substring.auth.auth_app_backend.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByname(String name);
}
