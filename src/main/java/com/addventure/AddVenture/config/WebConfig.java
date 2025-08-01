package com.addventure.AddVenture.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//Esta clase configura el manejo de recursos estáticos, como archivos subidos por los usuarios.
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    // Este método se encarga de registrar un manejador de recursos para servir
    // archivos subidos por los usuarios.
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
