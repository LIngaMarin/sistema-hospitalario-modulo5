package com.hospital.historia_clinica.service;

import com.hospital.historia_clinica.model.Rol;
import com.hospital.historia_clinica.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class RolService {

    private final RolRepository rolRepository;

    @Autowired
    public RolService(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    public List<Rol> listar() {
        return rolRepository.findAll();
    }

    public List<Rol> listarActivos() {
        return rolRepository.findByActivoTrue();
    }

    public Optional<Rol> buscarPorId(Long id) {
        return rolRepository.findById(id);
    }

    // Registrar rol: nuevo rol siempre queda activo
    public Rol registrar(Rol rol) {
        String nombre = normalizar(rol.getNombre());
        if (rolRepository.existsByNombre(nombre)) {
            throw new IllegalArgumentException("Ya existe un rol con el nombre " + nombre);
        }
        rol.setId(null);
        rol.setNombre(nombre);
        rol.setActivo(true);
        return rolRepository.save(rol);
    }

    // Editar rol: nombre y descripcion
    public Rol actualizar(Long id, Rol datos) {
        Rol existente = rolRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Rol no encontrado con id: " + id));
        String nombre = normalizar(datos.getNombre());
        if (rolRepository.existsByNombreAndIdNot(nombre, id)) {
            throw new IllegalArgumentException("Ya existe un rol con el nombre " + nombre);
        }
        existente.setNombre(nombre);
        existente.setDescripcion(datos.getDescripcion());
        return rolRepository.save(existente);
    }

    // Activar / desactivar rol
    public Rol cambiarEstado(Long id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Rol no encontrado con id: " + id));
        if (rol.isActivo() && "ADMINISTRADOR".equals(rol.getNombre())) {
            throw new IllegalStateException("El rol ADMINISTRADOR no se puede desactivar");
        }
        rol.setActivo(!rol.isActivo());
        return rolRepository.save(rol);
    }

    // ADMINISTRADOR, MEDICO, RECEPCIONISTA: sin espacios y sin el prefijo ROLE_ (Spring lo agrega solo)
    private String normalizar(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del rol es obligatorio");
        }
        String limpio = nombre.trim().toUpperCase().replace(" ", "_");
        if (limpio.startsWith("ROLE_")) {
            limpio = limpio.substring(5);
        }
        if (limpio.isEmpty()) {
            throw new IllegalArgumentException("El nombre del rol es obligatorio");
        }
        return limpio;
    }
}
