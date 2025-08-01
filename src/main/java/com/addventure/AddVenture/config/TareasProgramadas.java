package com.addventure.AddVenture.config;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.addventure.AddVenture.service.GrupoViajeService;

@Component
public class TareasProgramadas {

    private final GrupoViajeService grupoViajeService;

    @Autowired
    public TareasProgramadas(GrupoViajeService grupoViajeService) {
        this.grupoViajeService = grupoViajeService;
    }

    @Scheduled(cron = "0 0 2 * * ?", zone = "EST")
    public void actualizarCicloDeVidaDeGrupos() {
        System.out.println("--- EJECUTANDO TAREA PROGRAMADA: Actualizando estados de grupos @ " + LocalDateTime.now() + " ---");
        
        try {
            grupoViajeService.actualizarEstadosDeGrupos();
            System.out.println("--- TAREA PROGRAMADA FINALIZADA EXITOSAMENTE ---");
        } catch (Exception e) {
            System.err.println("--- ERROR EN TAREA PROGRAMADA: " + e.getMessage() + " ---");
        }
    }
    
}
