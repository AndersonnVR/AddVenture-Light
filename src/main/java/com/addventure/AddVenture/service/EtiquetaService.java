package com.addventure.AddVenture.service;

import java.util.Set;

import com.addventure.AddVenture.model.Etiqueta;

public interface EtiquetaService {
    
    Etiqueta crearOEncontrarEtiqueta(String nombreEtiqueta);

    Set<Etiqueta> procesarEtiquetas(Set<String> nombresEtiquetas);

}
