package com.hospital.historia_clinica.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.hospital.historia_clinica.model.Alergia;
import com.hospital.historia_clinica.service.AlergiaService;

@Controller
public class AlergiaController {

    private final AlergiaService alergiaService;

    public AlergiaController(AlergiaService alergiaService) {
        this.alergiaService = alergiaService;
    }

    @GetMapping("/alergias")
    public String listarAlergias(Model model) {
        model.addAttribute("alergias", alergiaService.listar());
        return "alergias";
    }

    @GetMapping("/alergias/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("alergia", new Alergia());
        return "alergia-form";
    }

    @PostMapping("/alergias/guardar")
    public String guardarAlergia(@ModelAttribute Alergia alergia) {
        alergiaService.guardar(alergia);
        return "redirect:/alergias";
    }

    @GetMapping("/alergias/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("alergia", alergiaService.obtener(id));
        return "alergia-form";
    }

    @GetMapping("/alergias/eliminar/{id}")
    public String eliminarAlergia(@PathVariable Long id) {
        alergiaService.eliminar(id);
        return "redirect:/alergias";
    }
}
