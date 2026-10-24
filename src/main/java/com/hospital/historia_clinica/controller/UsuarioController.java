package com.hospital.historia_clinica.controller;

import com.hospital.historia_clinica.model.Rol;
import com.hospital.historia_clinica.model.Usuario;
import com.hospital.historia_clinica.service.RolService;
import com.hospital.historia_clinica.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final RolService rolService;

    public UsuarioController(UsuarioService usuarioService, RolService rolService) {
        this.usuarioService = usuarioService;
        this.rolService = rolService;
    }

    @GetMapping("/usuarios")
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listar());
        return "usuarios";
    }

    @GetMapping("/usuarios/nuevo")
    public String nuevo(Model model) {
        Usuario u = new Usuario();
        u.setRol(new Rol());
        model.addAttribute("usuario", u);
        model.addAttribute("roles", rolService.listarActivos());
        return "usuario-form";
    }

    @GetMapping("/usuarios/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return usuarioService.buscarPorId(id).map(u -> {
            model.addAttribute("usuario", u);
            model.addAttribute("roles", rolService.listar());
            return "usuario-form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Usuario no encontrado");
            return "redirect:/usuarios";
        });
    }

    @PostMapping("/usuarios/guardar")
    public String guardar(@ModelAttribute Usuario usuario, RedirectAttributes ra) {
        try {
            usuarioService.registrar(usuario);
            ra.addFlashAttribute("mensaje", "Usuario registrado correctamente");
            return "redirect:/usuarios";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/usuarios/nuevo";
        }
    }

    @PostMapping("/usuarios/actualizar/{id}")
    public String actualizar(@PathVariable Long id, @ModelAttribute Usuario usuario, RedirectAttributes ra) {
        try {
            usuarioService.actualizar(id, usuario);
            ra.addFlashAttribute("mensaje", "Usuario actualizado correctamente");
            return "redirect:/usuarios";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/usuarios/editar/" + id;
        }
    }

    @PostMapping("/usuarios/estado/{id}")
    public String cambiarEstado(@PathVariable Long id, RedirectAttributes ra) {
        try {
            Usuario u = usuarioService.cambiarEstado(id);
            ra.addFlashAttribute("mensaje", "Usuario " + u.getUsername() + (u.isActivo() ? " activado" : " desactivado"));
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/usuarios";
    }
}
