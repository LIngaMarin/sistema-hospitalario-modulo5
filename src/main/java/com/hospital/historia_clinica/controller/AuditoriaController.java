package com.hospital.historia_clinica.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.hospital.historia_clinica.repository.AuditoriaRepository;

@Controller
public class AuditoriaController {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaController(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @GetMapping("/auditoria")
    public String listarAuditoria(Model model) {
        model.addAttribute("registros", auditoriaRepository.findAllByOrderByFechaHoraDesc());
        return "auditoria";
    }
}
