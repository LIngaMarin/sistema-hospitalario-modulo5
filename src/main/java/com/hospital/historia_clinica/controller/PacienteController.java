package com.hospital.historia_clinica.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hospital.historia_clinica.model.Paciente;
import com.hospital.historia_clinica.service.PacienteService;

@Controller
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @GetMapping("/pacientes")
    public String listarPacientes(Model model) {
        model.addAttribute("pacientes", pacienteService.listarTodos());
        return "pacientes";
    }

    @GetMapping("/pacientes/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("paciente", new Paciente());
        return "paciente-form";
    }

    @PostMapping("/pacientes/guardar")
    public String guardarPaciente(@ModelAttribute Paciente paciente, RedirectAttributes ra) {
        try {
            pacienteService.crear(paciente);
            ra.addFlashAttribute("mensaje", "Paciente registrado correctamente");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", "No se pudo registrar: el DNI ya existe o los datos son invalidos");
        }
        return "redirect:/pacientes";
    }

    @GetMapping("/pacientes/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return pacienteService.buscarPorId(id).map(p -> {
            model.addAttribute("paciente", p);
            return "paciente-form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Paciente no encontrado");
            return "redirect:/pacientes";
        });
    }

    @PostMapping("/pacientes/actualizar/{id}")
    public String actualizar(@PathVariable Long id, @ModelAttribute Paciente paciente, RedirectAttributes ra) {
        try {
            pacienteService.actualizar(id, paciente);
            ra.addFlashAttribute("mensaje", "Paciente actualizado correctamente");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pacientes";
    }

    @PostMapping("/pacientes/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            pacienteService.eliminar(id);
            ra.addFlashAttribute("mensaje", "Paciente eliminado correctamente");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pacientes";
    }
}
