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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

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
    // Configura las rutas públicas y protegidas, el inicio de sesión personalizado
    // y el manejo de cierre de sesión.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Desactivar CSRF para llamadas REST
                .csrf(csrf -> csrf.disable())

                .headers(headers -> headers
                        .cacheControl(cache -> cache.disable()) // desactiva caché
                )

                // Autorización
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos
                        .requestMatchers(
                                "/", "/registro", "/login", "/ingresar-code",
                                "/css/**", "/js/**", "/img/**", "/uploads/**",
                                "/api/auth/**" // API pública
                        ).permitAll()
                        // Todo lo demás requiere autenticación
                        .anyRequest().authenticated())

                // Login web tradicional
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/perfil", true)
                        .permitAll())

                // Logout
                .logout(logout -> logout
                        .logoutUrl("/logout") // URL para cerrar sesión
                        .logoutSuccessUrl("/") // redirige al login
                        .invalidateHttpSession(true)
                        .permitAll())
                // Para API REST: evita redirección al login HTML
                .httpBasic();

        http.addFilterBefore(new CodeValidationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // Autentica usuarios usando un servicio y un codificador de contraseñas.
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

    // Orquesta el proceso de autenticación en la aplicación.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Component
    public class CodeValidationFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain)
                throws ServletException, IOException {

            String path = request.getRequestURI();

            // 🔓 Rutas SIEMPRE públicas
            if (path.equals("/ingresar-code")
                    || path.equals("/login")
                    || path.equals("/registro")
                    || path.equals("/")
                    || path.equals("/logout")
                    || path.startsWith("/css")
                    || path.startsWith("/js")
                    || path.startsWith("/img")
                    || path.startsWith("/uploads")
                    || path.startsWith("/api")) {

                filterChain.doFilter(request, response);
                return;
            }

            // 🔒 Para rutas protegidas, verificar autenticación Y código
            HttpSession session = request.getSession(false);

            // Si no hay sesión o no está autenticado, dejar que Spring Security maneje
            if (session == null || request.getUserPrincipal() == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // Si está autenticado, verificar si validó el código
            boolean codeValid = Boolean.TRUE.equals(session.getAttribute("codeValid"));

            if (!codeValid) {
                System.out.println("⚠️ Usuario autenticado pero sin código validado. Redirigiendo desde: " + path);
                response.sendRedirect("/ingresar-code");
                return;
            }

            // ✅ Autenticado Y código válido
            filterChain.doFilter(request, response);
        }
    }
}
