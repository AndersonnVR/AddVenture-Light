package com.addventure.AddVenture.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import com.addventure.AddVenture.model.Usuario;
import com.addventure.AddVenture.repository.UsuarioRepository;

//Esta clase sirve para cargar los detalles del usuario durante el proceso de autenticación.
@Service
public class UsuarioDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Este método busca un usuario por su correo electrónico y devuelve un objeto UserDetails.
    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));
        return new UsuarioDetails(usuario);
    }
}
