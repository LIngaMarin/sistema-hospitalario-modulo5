package com.hospital.historia_clinica.controller;

import com.hospital.historia_clinica.model.CondicionMedica;
import com.hospital.historia_clinica.repository.CondicionMedicaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CondicionMedicaWebController {

    private final CondicionMedicaRepository condicionRepository;

    public CondicionMedicaWebController(CondicionMedicaRepository condicionRepository) {
        this.condicionRepository = condicionRepository;
    }

    @GetMapping("/condiciones")
    public String listar(Model model) {
        model.addAttribute("condiciones", condicionRepository.findAll());
        return "condiciones";
    }

    @PostMapping("/condiciones/guardar")
    public String guardar(@RequestParam String nombre, RedirectAttributes ra) {
        String limpio = nombre == null ? "" : nombre.trim();
        if (limpio.isEmpty()) {
            ra.addFlashAttribute("error", "El nombre es obligatorio");
        } else if (condicionRepository.findAll().stream().anyMatch(c -> c.getNombre().equalsIgnoreCase(limpio))) {
            ra.addFlashAttribute("error", "La condicion ya existe");
        } else {
            CondicionMedica c = new CondicionMedica();
            c.setNombre(limpio);
            condicionRepository.save(c);
            ra.addFlashAttribute("mensaje", "Condicion medica registrada");
        }
        return "redirect:/condiciones";
    }
}
