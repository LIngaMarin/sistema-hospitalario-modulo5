package com.hospital.historia_clinica.service;

import com.hospital.historia_clinica.model.HistoriaClinica;
import com.hospital.historia_clinica.model.Paciente;
import com.hospital.historia_clinica.repository.HistoriaClinicaRepository;
import com.hospital.historia_clinica.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class HistoriaClinicaService {

    private final HistoriaClinicaRepository historiaClinicaRepository;
    private final PacienteRepository pacienteRepository;
    private final com.hospital.historia_clinica.repository.CondicionMedicaRepository condicionMedicaRepository;

    @Autowired
    public HistoriaClinicaService(HistoriaClinicaRepository historiaClinicaRepository,
                                  PacienteRepository pacienteRepository,
                                  com.hospital.historia_clinica.repository.CondicionMedicaRepository condicionMedicaRepository) {
        this.historiaClinicaRepository = historiaClinicaRepository;
        this.pacienteRepository = pacienteRepository;
        this.condicionMedicaRepository = condicionMedicaRepository;
    }

    // RF-HC-01: Generar historia clínica única para un paciente
    public HistoriaClinica crear(Long pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado con id: " + pacienteId));

        // Genera un número único simple basado en el id del paciente y la fecha
        String numeroHistoria = "HC-" + String.format("%06d", pacienteId) + "-" + System.currentTimeMillis() % 10000;

        HistoriaClinica historia = new HistoriaClinica(
                numeroHistoria,
                paciente,
                LocalDate.now(),
                "ACTIVA"
        );

        return historiaClinicaRepository.save(historia);
    }

    // RF-HC-02: Consultar la historia clínica por su propio id
    public Optional<HistoriaClinica> buscarPorId(Long id) {
        return historiaClinicaRepository.findById(id);
    }

    // RF-HC-03: Mostrar historia clínica (con datos del paciente) a partir del id del paciente
    public Optional<HistoriaClinica> buscarPorPacienteId(Long pacienteId) {
        return historiaClinicaRepository.findByPacienteId(pacienteId);
    }

    public List<HistoriaClinica> listarTodas() {
        return historiaClinicaRepository.findAll();
    }

    // Para completar el CRUD (aunque no lo pida explícitamente el RF)
    public HistoriaClinica actualizar(Long id, String estado) {
        HistoriaClinica historia = historiaClinicaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Historia clínica no encontrada con id: " + id));
        if (!"ACTIVA".equals(estado) && !"INACTIVA".equals(estado)) {
            throw new IllegalArgumentException("Estado invalido: debe ser ACTIVA o INACTIVA");
        }
        historia.setEstado(estado);
        return historiaClinicaRepository.save(historia);
    }

    public void eliminar(Long id) {
        historiaClinicaRepository.deleteById(id);
    }

    public HistoriaClinica agregarCondicionFamiliar(Long historiaId, Long condicionId) {
        HistoriaClinica historia = historiaClinicaRepository.findById(historiaId)
                .orElseThrow(() -> new RuntimeException("Historia clínica no encontrada"));
        com.hospital.historia_clinica.model.CondicionMedica condicion = condicionMedicaRepository.findById(condicionId)
                .orElseThrow(() -> new RuntimeException("Condición médica no encontrada"));
        boolean yaAsignada = historia.getAntecedentesFamiliares().stream()
                .anyMatch(c -> c.getId().equals(condicionId));
        if (yaAsignada) {
            throw new IllegalArgumentException("La condicion ya esta asignada a esta historia");
        }
        historia.agregarCondicion(condicion);
        return historiaClinicaRepository.save(historia);
    }

    public HistoriaClinica eliminarCondicionFamiliar(Long historiaId, Long condicionId) {
        HistoriaClinica historia = historiaClinicaRepository.findById(historiaId)
                .orElseThrow(() -> new RuntimeException("Historia clínica no encontrada"));
        historia.getAntecedentesFamiliares().removeIf(c -> c.getId().equals(condicionId));
        return historiaClinicaRepository.save(historia);
    }

    public java.util.List<com.hospital.historia_clinica.model.CondicionMedica> listarCondicionesFamiliares(Long historiaId) {
        HistoriaClinica historia = historiaClinicaRepository.findById(historiaId)
                .orElseThrow(() -> new RuntimeException("Historia clínica no encontrada"));
        return historia.getAntecedentesFamiliares();
    }

}