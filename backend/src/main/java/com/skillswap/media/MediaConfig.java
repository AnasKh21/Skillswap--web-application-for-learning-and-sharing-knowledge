package com.skillswap.media;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

// Sert les fichiers uploadés (avatars, bannières) comme ressources statiques.
// GET /uploads/avatars/{userId}.jpg → fichier sur le disque dans ./uploads/avatars/
@Configuration
public class MediaConfig implements WebMvcConfigurer {

    @Value("${app.media.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = Paths.get(uploadDir).toAbsolutePath() + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath)
                .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS));
    }
}
