package com.hospital.historia_clinica.repository;

import com.hospital.historia_clinica.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, Long id);

    // Sirve para no dejar el sistema sin ningun administrador activo
    long countByRolNombreAndActivoTrue(String nombreRol);
}
