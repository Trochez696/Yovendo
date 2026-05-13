package com.yovendo.backend.service;

import com.yovendo.backend.dto.RoleDTO;
import com.yovendo.backend.dto.UserDTO;
import com.yovendo.backend.entity.Role;
import com.yovendo.backend.entity.User;
import com.yovendo.backend.repository.RoleRepository;
import com.yovendo.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    // Repositorios para usuarios y roles.
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    // Codificador BCrypt para no guardar contrasenas en texto plano.
    private final PasswordEncoder passwordEncoder;

    // Lista usuarios existentes sin exponer contrasenas.
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserDTO)
                .toList();
    }

    // Consulta un usuario por id.
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return toUserDTO(user);
    }

    @Transactional
    public UserDTO createUser(String username, String password, String email, List<String> roleNames) {
        // Evita duplicados y guarda la contrasena cifrada antes de persistir.
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("El nombre de usuario ya existe");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("El email ya está en uso");
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .active(true)
                .build();

        List<Role> roles = roleRepository.findAll().stream()
                .filter(r -> roleNames.contains(r.getName()))
                .toList();
        user.setRoles(java.util.Set.copyOf(roles));

        user = userRepository.save(user);
        return toUserDTO(user);
    }

    @Transactional
    public UserDTO updateUser(Long id, String username, String email, List<String> roleNames) {
        // Permite modificar datos basicos y roles sin cambiar la contrasena.
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (username != null && !username.equals(user.getUsername())) {
            if (userRepository.existsByUsername(username)) {
                throw new RuntimeException("El nombre de usuario ya existe");
            }
            user.setUsername(username);
        }

        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new RuntimeException("El email ya está en uso");
            }
            user.setEmail(email);
        }

        if (roleNames != null && !roleNames.isEmpty()) {
            List<Role> roles = roleRepository.findAll().stream()
                    .filter(r -> roleNames.contains(r.getName()))
                    .toList();
            user.setRoles(java.util.Set.copyOf(roles));
        }

        user = userRepository.save(user);
        return toUserDTO(user);
    }

    @Transactional
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setActive(true);
        userRepository.save(user);
    }

    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado");
        }
        userRepository.deleteById(id);
    }

    public List<RoleDTO> getAllRoles() {
        // Roles disponibles en la aplicacion.
        return roleRepository.findAll().stream()
                .map(this::toRoleDTO)
                .toList();
    }

    @Transactional
    public RoleDTO createRole(String name, String description) {
        // No permite crear dos roles con el mismo nombre.
        if (roleRepository.existsByName(name)) {
            throw new RuntimeException("El rol ya existe");
        }
        Role role = Role.builder()
                .name(name)
                .description(description)
                .build();
        role = roleRepository.save(role);
        return toRoleDTO(role);
    }

    private UserDTO toUserDTO(User user) {
        // Convierte User a DTO y transforma Set<Role> en lista de nombres.
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .active(user.isEnabled())
                .roles(user.getRoles().stream().map(Role::getName).toList())
                .build();
    }

    private RoleDTO toRoleDTO(Role role) {
        // Convierte Role a DTO para respuestas de la API.
        return RoleDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build();
    }
}
