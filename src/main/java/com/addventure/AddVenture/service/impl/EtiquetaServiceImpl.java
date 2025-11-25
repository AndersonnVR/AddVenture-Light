package com.addventure.AddVenture.service.impl;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.addventure.AddVenture.model.Etiqueta;
import com.addventure.AddVenture.repository.EtiquetaRepository;
import com.addventure.AddVenture.service.EtiquetaService;

@Service
public class EtiquetaServiceImpl implements EtiquetaService {

    private final EtiquetaRepository etiquetaRepository;

    @Autowired
    public EtiquetaServiceImpl(EtiquetaRepository etiquetaRepository) {
        this.etiquetaRepository = etiquetaRepository;
    }

    @Override
    @Transactional
    public Etiqueta crearOEncontrarEtiqueta(String nombreEtiqueta) {

        String nombreLimpio = nombreEtiqueta.trim().toLowerCase();
        if (nombreLimpio.startsWith("#")) {
            nombreLimpio = nombreLimpio.substring(1);
        }

        Etiqueta etiquetaExistente = etiquetaRepository.findByNombreEtiqueta(nombreLimpio);

        if (etiquetaExistente != null) {
            return etiquetaExistente;
        } else {
            Etiqueta nuevaEtiqueta = Etiqueta.builder()
                                        .nombreEtiqueta(nombreLimpio)
                                        .build();
            return etiquetaRepository.save(nuevaEtiqueta);
        }
    }

    @Override
    @Transactional
    public Set<Etiqueta> procesarEtiquetas(Set<String> nombresEtiquetas) {
        
        Set<Etiqueta> etiquetasProcesadas = new HashSet<>();

        if (nombresEtiquetas != null && !nombresEtiquetas.isEmpty()) {
            for (String nombre : nombresEtiquetas) {
                etiquetasProcesadas.add(crearOEncontrarEtiqueta(nombre));
            }
        }
        
        return etiquetasProcesadas;
    }
    
}
