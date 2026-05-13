package com.yovendo.backend.config;

import com.yovendo.backend.entity.Role;
import com.yovendo.backend.entity.User;
import com.yovendo.backend.repository.RoleRepository;
import com.yovendo.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Datos base necesarios para que el sistema pueda iniciar con roles conocidos.
        createRoleIfNotExists("ADMIN", "Administrador del sistema");
        createRoleIfNotExists("DIRECTOR", "Director de ventas");
        createRoleIfNotExists("CONSULTOR", "Consultor de ventas");
        createRoleIfNotExists("SUPERVISOR", "Supervisor de inventario");

        // Usuario inicial para entrar por primera vez al panel de administracion.
        if (!userRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@yovendo.com")
                    .active(true)
                    .roles(Set.of(adminRole))
                    .build();
            userRepository.save(admin);
        }
    }

    private void createRoleIfNotExists(String name, String description) {
        if (!roleRepository.existsByName(name)) {
            Role role = Role.builder()
                    .name(name)
                    .description(description)
                    .build();
            roleRepository.save(role);
        }
    }
}
