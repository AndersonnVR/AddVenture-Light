package com.addventure.AddVenture.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.addventure.AddVenture.dto.RegistroUsuarioDTO;
import com.addventure.AddVenture.model.Logro;
import com.addventure.AddVenture.model.Usuario;
import com.addventure.AddVenture.repository.LogroRepository;
import com.addventure.AddVenture.repository.UsuarioRepository;

// Esta clase maneja la lógica de negocio relacionada con los usuarios, 
//como el registro y la verificación de existencia de correos y nombres de usuario.
@Service
public class UsuarioService {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LogroRepository logroRepository;

    // verifica si un correo electrónico ya está registrado en la base de datos.
    public boolean existeCorreo(String correo) {
        return usuarioRepository.existsByCorreo(correo);
    }

    // verifica si un nombre de usuario ya está registrado en la base de datos.
    public boolean existeNombreUsuario(String nombreUsuario) {
        return usuarioRepository.existsByNombreUsuario(nombreUsuario);
    }

    private final String UPLOAD_DIR = "uploads";

    public String guardarImagenTemporal(MultipartFile archivo) {
        if (archivo != null && !archivo.isEmpty()) {
            try {
                String nombreArchivo = UUID.randomUUID().toString() + "_" + archivo.getOriginalFilename();
                Path rutaDestino = Paths.get(UPLOAD_DIR).resolve(nombreArchivo).toAbsolutePath();
                Files.createDirectories(rutaDestino.getParent());
                archivo.transferTo(rutaDestino);
                return nombreArchivo;
            } catch (IOException e) {
                throw new RuntimeException("Error al guardar la imagen de perfil: " + e.getMessage());
            }
        }
        return "perfil_defecto.png"; // Retorna imagen por defecto si no hay archivo
    }

    // Este método registra un nuevo usuario en la base de datos.
    public Usuario registrarUsuarioFinal(RegistroUsuarioDTO dto, String nombreImagenGuardada) {
        Usuario nuevo = new Usuario();

        //Datos básicos
        nuevo.setNombre(dto.getNombre());
        nuevo.setApellido(dto.getApellido());
        nuevo.setNombreUsuario(dto.getNombreUsuario());
        nuevo.setDescripcion(dto.getDescripcion());
        nuevo.setPais(dto.getPais());
        nuevo.setCiudad(dto.getCiudad());
        nuevo.setFechaNacimiento(dto.getFechaNacimiento());
        nuevo.setCorreo(dto.getCorreo());
        nuevo.setContrasenia(passwordEncoder.encode(dto.getContrasenia()));
        nuevo.setRol("ROLE_USER");
        nuevo.setFotoPerfil(nombreImagenGuardada);

         // Buscar el logro base "Miembro de la comunidad"
        Logro logroComunidad = logroRepository.findByNombre("Miembro de la comunidad")
                .orElseThrow(() -> new RuntimeException("Logro base no encontrado"));

        // Asignarlo al nuevo usuario
        nuevo.getLogros().add(logroComunidad);

        return usuarioRepository.save(nuevo);
    }
}
