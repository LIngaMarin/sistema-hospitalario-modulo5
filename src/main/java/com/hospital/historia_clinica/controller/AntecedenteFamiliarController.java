package com.hospital.historia_clinica.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.hospital.historia_clinica.model.AntecedenteFamiliar;
import com.hospital.historia_clinica.service.AntecedenteFamiliarService;

@Controller
public class AntecedenteFamiliarController {

    private final AntecedenteFamiliarService antecedenteFamiliarService;

    public AntecedenteFamiliarController(AntecedenteFamiliarService antecedenteFamiliarService) {
        this.antecedenteFamiliarService = antecedenteFamiliarService;
    }

    @GetMapping("/antecedentes-familiares")
    public String listarAntecedentesFamiliares(Model model) {
        model.addAttribute("antecedentes", antecedenteFamiliarService.listar());
        return "antecedentes-familiares";
    }

    @GetMapping("/antecedentes-familiares/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("antecedente", new AntecedenteFamiliar());
        return "antecedente-familiar-form";
    }

    @PostMapping("/antecedentes-familiares/guardar")
    public String guardarAntecedenteFamiliar(@ModelAttribute AntecedenteFamiliar antecedente) {
        antecedenteFamiliarService.guardar(antecedente);
        return "redirect:/antecedentes-familiares";
    }

    @GetMapping("/antecedentes-familiares/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("antecedente", antecedenteFamiliarService.obtener(id));
        return "antecedente-familiar-form";
    }

    @GetMapping("/antecedentes-familiares/eliminar/{id}")
    public String eliminarAntecedenteFamiliar(@PathVariable Long id) {
        antecedenteFamiliarService.eliminar(id);
        return "redirect:/antecedentes-familiares";
    }
}
