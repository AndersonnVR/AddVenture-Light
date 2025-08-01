package com.addventure.AddVenture.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.addventure.AddVenture.model.GrupoViaje;
import com.addventure.AddVenture.service.BuscarGrupoService;

@Controller
public class BuscarGrupoController {

    private final BuscarGrupoService buscarGrupoService;

    @Autowired
    public BuscarGrupoController(BuscarGrupoService buscarGrupoService) {
        this.buscarGrupoService = buscarGrupoService;
    }

    @GetMapping("/buscar-grupos")
    public String MostrarGrupos(
            @RequestParam(required = false) String destinoPrincipal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(required = false) String sort,
            Model model){

        if (destinoPrincipal != null && !destinoPrincipal.isBlank() && !destinoPrincipal.matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s]+$")) {
            model.addAttribute("error", "Se deben ingresar letras");
            model.addAttribute("grupos", List.of());
            return "buscar_grupos";
        }

        LocalDate hoy = LocalDate.now();
        if((fechaInicio != null && fechaInicio.isBefore(hoy)) || (fechaFin != null && fechaFin.isBefore(hoy))){
            model.addAttribute("error", "Fecha invalida!");
            model.addAttribute("grupos", List.of());
            return "buscar_grupos";
        }

        Pageable pageable;
        if(sort != null && !sort.isBlank()){
            pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        } else{
            pageable = PageRequest.of(page, size);
        }
        
        Page<GrupoViaje> paginaGrupos;
        if((destinoPrincipal == null || destinoPrincipal.isBlank()) && fechaInicio == null && fechaFin == null){
            paginaGrupos = buscarGrupoService.obtenerGrupos(pageable);
        } else{
            paginaGrupos = buscarGrupoService.buscarGrupos(destinoPrincipal, fechaInicio, fechaFin, pageable);
        }

        model.addAttribute("grupos", paginaGrupos.getContent());
        model.addAttribute("totalPages", paginaGrupos.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);

        return "buscar_grupos";
    }
    
}
