package com.hospital.historia_clinica.controller;

import com.hospital.historia_clinica.model.Rol;
import com.hospital.historia_clinica.service.RolService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/roles")
public class RolRestController {

    private final RolService rolService;

    public RolRestController(RolService rolService) {
        this.rolService = rolService;
    }

    @PostMapping
    public ResponseEntity<Rol> registrar(@RequestBody Rol rol) {
        return new ResponseEntity<>(rolService.registrar(rol), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Rol>> listar() {
        return ResponseEntity.ok(rolService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rol> buscar(@PathVariable Long id) {
        return rolService.buscarPorId(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Rol> actualizar(@PathVariable Long id, @RequestBody Rol rol) {
        return ResponseEntity.ok(rolService.actualizar(id, rol));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Rol> cambiarEstado(@PathVariable Long id) {
        return ResponseEntity.ok(rolService.cambiarEstado(id));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> noEncontrado(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<String> invalido(RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
