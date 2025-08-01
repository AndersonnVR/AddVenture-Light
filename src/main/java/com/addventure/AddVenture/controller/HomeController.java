package com.addventure.AddVenture.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Esta clase maneja las peticiones a la página de inicio
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }
}
