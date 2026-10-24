package com.hospital.historia_clinica.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hospital.historia_clinica.model.Auditoria;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    List<Auditoria> findAllByOrderByFechaHoraDesc();
}
