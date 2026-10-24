package com.hospital.historia_clinica.service;

import com.hospital.historia_clinica.model.Paciente;
import com.hospital.historia_clinica.repository.HistoriaClinicaRepository;
import com.hospital.historia_clinica.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final HistoriaClinicaRepository historiaClinicaRepository;

    @Autowired
    public PacienteService(PacienteRepository pacienteRepository,
                           HistoriaClinicaRepository historiaClinicaRepository) {
        this.pacienteRepository = pacienteRepository;
        this.historiaClinicaRepository = historiaClinicaRepository;
    }

    public Paciente crear(Paciente paciente) {
        return pacienteRepository.save(paciente);
    }

    public List<Paciente> listarTodos() {
        return pacienteRepository.findAll();
    }

    public Optional<Paciente> buscarPorId(Long id) {
        return pacienteRepository.findById(id);
    }

    // Editar datos del paciente
    public Paciente actualizar(Long id, Paciente datos) {
        Paciente existente = pacienteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Paciente no encontrado con id: " + id));
        if (datos.getDni() != null && pacienteRepository.existsByDniAndIdNot(datos.getDni(), id)) {
            throw new IllegalArgumentException("Ya existe otro paciente con el DNI " + datos.getDni());
        }
        existente.setNombres(datos.getNombres());
        existente.setApellidos(datos.getApellidos());
        existente.setDni(datos.getDni());
        return pacienteRepository.save(existente);
    }

    // Eliminar paciente: no se permite si ya tiene historia clinica
    public void eliminar(Long id) {
        if (!pacienteRepository.existsById(id)) {
            throw new NoSuchElementException("Paciente no encontrado con id: " + id);
        }
        if (historiaClinicaRepository.findByPacienteId(id).isPresent()) {
            throw new IllegalStateException("No se puede eliminar: el paciente ya tiene una historia clinica");
        }
        pacienteRepository.deleteById(id);
    }
}
