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

        // Formatear el nombre de la etiqueta
        String nombreLimpio = nombreEtiqueta.trim().toLowerCase();
        if (nombreLimpio.startsWith("#")) {
            nombreLimpio = nombreLimpio.substring(1);
        }

        // Intentar encontrar la etiqueta por su nombre
        Etiqueta etiquetaExistente = etiquetaRepository.findByNombreEtiqueta(nombreLimpio);

        // Si la etiqueta existe, la devolvemos
        if (etiquetaExistente != null) {
            return etiquetaExistente;
        } else {
            // Si no existe, creamos una nueva y la guardamos
            Etiqueta nuevaEtiqueta = Etiqueta.builder()
                                        .nombreEtiqueta(nombreLimpio)
                                        .build();
            return etiquetaRepository.save(nuevaEtiqueta);
        }
    }

    @Override
    @Transactional
    public Set<Etiqueta> procesarEtiquetas(Set<String> nombresEtiquetas) {

        // Crear un conjunto vacío para almacenar las etiquetas procesadas
        Set<Etiqueta> etiquetasProcesadas = new HashSet<>();

        // Verificar que el conjunto de nombres de etiquetas no sea nulo o vacío
        if (nombresEtiquetas != null && !nombresEtiquetas.isEmpty()) {
            for (String nombre : nombresEtiquetas) {
                // Llamar al método crearOEncontrarEtiqueta para asegurar que cada etiqueta sea única y esté persistida.
                etiquetasProcesadas.add(crearOEncontrarEtiqueta(nombre));
            }
        }
        return etiquetasProcesadas;
    }
    
}
