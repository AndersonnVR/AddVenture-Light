package com.addventure.AddVenture.service;

import com.addventure.AddVenture.model.Usuario;

public interface ParticipanteGrupoService {
    
    void unirseAGrupo(Long idGrupo, Usuario usuarioActual);

    void salirseDeGrupo(Long idGrupo, Usuario usuarioActual);

    void eliminarParticipanteDelGrupo(Long idGrupo, Long idParticipante, Usuario creador);

    boolean esUsuarioParticipante(Long idGrupo, Usuario usuarioActual);

}
