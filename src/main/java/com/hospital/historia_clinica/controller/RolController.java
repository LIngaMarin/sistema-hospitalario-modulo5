package com.hospital.historia_clinica.controller;

import com.hospital.historia_clinica.model.Rol;
import com.hospital.historia_clinica.service.RolService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping("/roles")
    public String listar(Model model) {
        model.addAttribute("roles", rolService.listar());
        return "roles";
    }

    @GetMapping("/roles/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("rol", new Rol());
        return "rol-form";
    }

    @GetMapping("/roles/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return rolService.buscarPorId(id).map(r -> {
            model.addAttribute("rol", r);
            return "rol-form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Rol no encontrado");
            return "redirect:/roles";
        });
    }

    @PostMapping("/roles/guardar")
    public String guardar(@ModelAttribute Rol rol, RedirectAttributes ra) {
        try {
            rolService.registrar(rol);
            ra.addFlashAttribute("mensaje", "Rol registrado correctamente");
            return "redirect:/roles";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/roles/nuevo";
        }
    }

    @PostMapping("/roles/actualizar/{id}")
    public String actualizar(@PathVariable Long id, @ModelAttribute Rol rol, RedirectAttributes ra) {
        try {
            rolService.actualizar(id, rol);
            ra.addFlashAttribute("mensaje", "Rol actualizado correctamente");
            return "redirect:/roles";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/roles/editar/" + id;
        }
    }

    @PostMapping("/roles/estado/{id}")
    public String cambiarEstado(@PathVariable Long id, RedirectAttributes ra) {
        try {
            Rol r = rolService.cambiarEstado(id);
            ra.addFlashAttribute("mensaje", "Rol " + r.getNombre() + (r.isActivo() ? " activado" : " desactivado"));
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/roles";
    }
}
