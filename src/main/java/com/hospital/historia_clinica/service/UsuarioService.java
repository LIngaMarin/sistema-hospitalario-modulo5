package com.hospital.historia_clinica.service;

import com.hospital.historia_clinica.model.Rol;
import com.hospital.historia_clinica.model.Usuario;
import com.hospital.historia_clinica.repository.RolRepository;
import com.hospital.historia_clinica.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UsuarioService {

    private static final String ROL_ADMIN = "ADMINISTRADOR";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    // Registrar usuario con su rol; la contrasena se guarda encriptada con BCrypt
    public Usuario registrar(Usuario usuario) {
        validarDatos(usuario);
        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contrasena es obligatoria");
        }
        String username = usuario.getUsername().trim();
        if (usuarioRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Ya existe un usuario con el username " + username);
        }
        Rol rol = buscarRol(usuario.getRol());
        if (!rol.isActivo()) {
            throw new IllegalArgumentException("El rol seleccionado esta inactivo");
        }
        usuario.setId(null);
        usuario.setUsername(username);
        usuario.setRol(rol);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }

    // Editar usuario: si la contrasena llega vacia se conserva la anterior
    public Usuario actualizar(Long id, Usuario datos) {
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con id: " + id));
        validarDatos(datos);
        String username = datos.getUsername().trim();
        if (usuarioRepository.existsByUsernameAndIdNot(username, id)) {
            throw new IllegalArgumentException("Ya existe un usuario con el username " + username);
        }

        Rol nuevoRol = buscarRol(datos.getRol());
        boolean cambiaRol = !nuevoRol.getId().equals(existente.getRol().getId());
        if (cambiaRol && !nuevoRol.isActivo()) {
            throw new IllegalArgumentException("El rol seleccionado esta inactivo");
        }
        if (cambiaRol && esUltimoAdministradorActivo(existente)) {
            throw new IllegalStateException("No se puede cambiar el rol del ultimo ADMINISTRADOR activo");
        }

        existente.setUsername(username);
        existente.setNombreCompleto(datos.getNombreCompleto());
        existente.setEmail(datos.getEmail());
        existente.setRol(nuevoRol);
        if (datos.getPassword() != null && !datos.getPassword().isBlank()) {
            existente.setPassword(passwordEncoder.encode(datos.getPassword()));
        }
        return usuarioRepository.save(existente);
    }

    // Activar / desactivar usuario
    public Usuario cambiarEstado(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con id: " + id));
        if (usuario.isActivo() && esUltimoAdministradorActivo(usuario)) {
            throw new IllegalStateException("No se puede desactivar al ultimo ADMINISTRADOR activo");
        }
        usuario.setActivo(!usuario.isActivo());
        return usuarioRepository.save(usuario);
    }

    private void validarDatos(Usuario usuario) {
        if (usuario.getUsername() == null || usuario.getUsername().isBlank()) {
            throw new IllegalArgumentException("El username es obligatorio");
        }
        if (usuario.getNombreCompleto() == null || usuario.getNombreCompleto().isBlank()) {
            throw new IllegalArgumentException("El nombre completo es obligatorio");
        }
        if (usuario.getRol() == null || usuario.getRol().getId() == null) {
            throw new IllegalArgumentException("Debe seleccionar un rol");
        }
    }

    private Rol buscarRol(Rol rol) {
        return rolRepository.findById(rol.getId())
                .orElseThrow(() -> new NoSuchElementException("Rol no encontrado con id: " + rol.getId()));
    }

    private boolean esUltimoAdministradorActivo(Usuario usuario) {
        return usuario.isActivo()
                && ROL_ADMIN.equals(usuario.getRol().getNombre())
                && usuarioRepository.countByRolNombreAndActivoTrue(ROL_ADMIN) <= 1;
    }
}
