package fr.takima.training.simpleapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // ✅ Applique CORS sur toutes les routes
                        .allowedOrigins("*") // ✅ Accepte toutes les origines
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // ✅ Toutes les méthodes HTTP
                        .allowedHeaders("*"); // ✅ Tous les headers sont acceptés
            }
        };
    }
}
