package com.addventure.AddVenture.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.addventure.AddVenture.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
    
    boolean existsByNombreUsuario(String nombreUsuario);

    Usuario findByNombreUsuario(String nombreUsuario);
    
}
