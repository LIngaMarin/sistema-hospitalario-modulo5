package com.hospital.historia_clinica.config;

import com.hospital.historia_clinica.model.Rol;
import com.hospital.historia_clinica.model.Usuario;
import com.hospital.historia_clinica.repository.RolRepository;
import com.hospital.historia_clinica.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(RolRepository rolRepository, UsuarioRepository usuarioRepository,
                      PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Rol admin = crearRol("ADMINISTRADOR", "Acceso total al sistema");
        Rol medico = crearRol("MEDICO", "Consulta y registra informacion clinica");
        Rol recepcion = crearRol("RECEPCIONISTA", "Registra pacientes y genera historias");

        crearUsuario("admin", "admin123", "Administrador del sistema", "admin@hospital.com", admin);
        crearUsuario("medico", "medico123", "Medico de prueba", "medico@hospital.com", medico);
        crearUsuario("recepcion", "recepcion123", "Recepcionista de prueba", "recepcion@hospital.com", recepcion);
    }

    private Rol crearRol(String nombre, String descripcion) {
        return rolRepository.findByNombre(nombre)
                .orElseGet(() -> rolRepository.save(new Rol(nombre, descripcion)));
    }

    private void crearUsuario(String username, String password, String nombre, String email, Rol rol) {
        if (!usuarioRepository.existsByUsername(username)) {
            usuarioRepository.save(new Usuario(username, passwordEncoder.encode(password), nombre, email, rol));
        }
    }
}
