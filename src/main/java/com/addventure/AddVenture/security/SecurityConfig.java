package com.addventure.AddVenture.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.addventure.AddVenture.handler.CustomLoginSuccessHandler;

//Esta clase configura la seguridad de la aplicación, incluyendo autenticación y autorización.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UsuarioDetailsService usuarioDetailsService;
    @Autowired
    private CustomLoginSuccessHandler customLoginSuccessHandler;

    // Este método define la cadena de filtros de seguridad para la aplicación.
    // Configura las rutas públicas y protegidas, el inicio de sesión personalizado y el manejo de cierre de sesión.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                    .ignoringRequestMatchers("/sendMessageFile", "/sendMessage") 
                )
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                            "/", 
                                        "/registro", 
                                        "/verificar",        // <--- ¡ESTO ES CRUCIAL!
                                        "/reenviar-codigo",  
                                        "/sendMessageFile", "/sendMessage",
                                        "/login", 
                                        "/css/**", "/js/**", "/img/**", "/uploads/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(customLoginSuccessHandler)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());

        return http.build();
    }

    //Autentica usuarios usando un servicio y un codificador de contraseñas.
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(usuarioDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // Codifica y verifica contraseñas usando BCrypt.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //Orquesta el proceso de autenticación en la aplicación.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
