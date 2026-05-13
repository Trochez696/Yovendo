package com.yovendo.backend.repository;

import com.yovendo.backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    // El nombre del rol es la clave logica usada por seguridad y frontend.
    Optional<Role> findByName(String name);
    boolean existsByName(String name);
}
