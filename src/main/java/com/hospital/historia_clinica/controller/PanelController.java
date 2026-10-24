package com.hospital.historia_clinica.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PanelController {

    @GetMapping({"/", "/panel"})
    public String panel(Authentication auth, Model model) {
        // Rol del usuario autenticado, para mostrar solo las opciones que le corresponden
        String rol = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        model.addAttribute("usuario", auth.getName());
        model.addAttribute("rol", rol);
        model.addAttribute("esAdmin", rol.equals("ADMINISTRADOR"));
        model.addAttribute("esMedico", rol.equals("MEDICO"));
        model.addAttribute("esRecepcionista", rol.equals("RECEPCIONISTA"));
        return "panel";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/acceso-denegado")
    public String accesoDenegado() {
        return "acceso-denegado";
    }
}
