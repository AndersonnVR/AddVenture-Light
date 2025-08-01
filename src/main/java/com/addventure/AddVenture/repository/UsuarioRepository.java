package com.addventure.AddVenture.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.addventure.AddVenture.model.Usuario;

@Repository
//Esta interfaz permite realizar operaciones CRUD sobre la entidad Usuario sin escribir queries SQL.
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Busca un usuario por medio de su correo.
    Optional<Usuario> findByCorreo(String correo);

    // Busca si ya hay un usuario registrado con un determinado correo.
    boolean existsByCorreo(String correo);
    // Busca si ya hay un usuario registrado con un determinado nombre de usuario.
    boolean existsByNombreUsuario(String nombreUsuario);

    Usuario findByNombreUsuario(String nombreUsuario);
    
}
