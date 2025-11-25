package com.addventure.AddVenture.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.addventure.AddVenture.model.Usuario;
import com.addventure.AddVenture.repository.OpinionRepository;
import com.addventure.AddVenture.repository.UsuarioRepository;
import com.addventure.AddVenture.service.GrupoViajeService;

import java.security.Principal;

@Controller
public class PerfilController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private OpinionRepository opinionRepository;

    @Autowired
    private GrupoViajeService grupoViajeService;

    @GetMapping("/perfil")
    public String index(Model model, Principal principal) {

        String correo = principal.getName();
        Usuario usuarioPrincipal = usuarioRepository.findByCorreo(correo)
                .orElse(null);

        if (usuarioPrincipal == null) {
            return "redirect:/login?error";
        }

         model.addAttribute("usuario", usuarioPrincipal);
        model.addAttribute("logros", usuarioPrincipal.getLogros());
        model.addAttribute("pasaportes", grupoViajeService.obtenerGruposFinalizadosParaPasaporte(usuarioPrincipal));
        model.addAttribute("opiniones", opinionRepository.findByDestinatario(usuarioPrincipal));
        model.addAttribute("proximosViajes", grupoViajeService.obtenerGruposActivosParaUsuario(usuarioPrincipal));

        return "perfil";
    }
    
    @GetMapping("/perfil/{nombreUsuario}")
    public String verPerfilPublico(@PathVariable String nombreUsuario,
                                   Model model) {

        Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuario);

        if (usuario == null) {
            return "redirect:/404";
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("logros", usuario.getLogros());
        model.addAttribute("pasaportes", grupoViajeService.obtenerGruposFinalizadosParaPasaporte(usuario));
        model.addAttribute("opiniones", opinionRepository.findByDestinatario(usuario));
        model.addAttribute("proximosViajes", grupoViajeService.obtenerGruposActivosParaUsuario(usuario));

        return "perfil";
    }

}