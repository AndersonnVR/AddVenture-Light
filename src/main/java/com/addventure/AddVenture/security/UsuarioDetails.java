package com.addventure.AddVenture.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.addventure.AddVenture.model.Usuario;

import java.util.Collection;
import java.util.List;

// Esta clase implementa UserDetails para proporcionar detalles del usuario autenticado y sus roles. 
// Es utilizada por Spring Security para manejar la autenticación y autorización.
public class UsuarioDetails implements UserDetails {

    private final Usuario usuario;

    public UsuarioDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    // Devuelve una colección de autoridades (roles) del usuario.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(usuario.getRol()));
    }

    @Override
    public String getPassword() {
        return usuario.getContrasenia();
    }

    @Override
    public String getUsername() {
        return usuario.getCorreo();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}
