package com.addventure.AddVenture.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class VerificacionController {
    
    @GetMapping("/verification-email")
    public String mostrarVerificacionEmail() {
        return "verification-email"; // busca en templates
    }
    
    @GetMapping("/ingresar-code")
    public String mostrarIngresoCode() {
        return "ingresar-code"; // busca en templates
    }

    @GetMapping("/verificar-codigo")
    public String mostrarVerificacionCodigo() {
        return "verificar-codigo"; // busca en templates    
    }
    

}

