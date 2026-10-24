package com.hospital.historia_clinica.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.hospital.historia_clinica.model.Antecedente;
import com.hospital.historia_clinica.service.AntecedenteService;

@Controller
public class AntecedenteController {

    private final AntecedenteService antecedenteService;

    public AntecedenteController(AntecedenteService antecedenteService) {
        this.antecedenteService = antecedenteService;
    }

    @GetMapping("/antecedentes")
    public String listarAntecedentes(Model model) {
        model.addAttribute("antecedentes", antecedenteService.listar());
        return "antecedentes";
    }

    @GetMapping("/antecedentes/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("antecedente", new Antecedente());
        return "antecedente-form";
    }

    @PostMapping("/antecedentes/guardar")
    public String guardarAntecedente(@ModelAttribute Antecedente antecedente) {
        antecedenteService.guardar(antecedente);
        return "redirect:/antecedentes";
    }

    @GetMapping("/antecedentes/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("antecedente", antecedenteService.obtener(id));
        return "antecedente-form";
    }

    @GetMapping("/antecedentes/eliminar/{id}")
    public String eliminarAntecedente(@PathVariable Long id) {
        antecedenteService.eliminar(id);
        return "redirect:/antecedentes";
    }
}
