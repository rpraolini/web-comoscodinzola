package it.asso.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import java.io.IOException;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.base-path}")
    private String uploadBasePath;

    // Questo serve per mappare la root "/" direttamente a index.html
    /* @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Opzionale se gestito sotto, ma male non fa
        registry.addViewController("/").setViewName("forward:/index.html");
    } */

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve le foto degli animali dal filesystem locale
        String location = uploadBasePath.endsWith("/") || uploadBasePath.endsWith("\\")
                ? "file:" + uploadBasePath
                : "file:" + uploadBasePath + "/";
        registry.addResourceHandler("/images/**")
                .addResourceLocations(location);

        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);

                        // 1. CASO FELICE: Il file esiste (es. main.js, logo.png) -> Servilo
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }

                        // 2. CASO ERRORE VERO: Il file manca ma è un asset statico (.js, .css, img)
                        // Se non lo trovi, restituisci NULL (404) invece di index.html
                        // Questo evita l'errore "MIME type text/html" e ti fa vedere il vero 404 in console
                        if (resourcePath.endsWith(".js") || resourcePath.endsWith(".css") ||
                                resourcePath.endsWith(".png") || resourcePath.endsWith(".jpg") ||
                                resourcePath.endsWith(".ico") || resourcePath.endsWith(".json")||
                                resourcePath.endsWith(".woff") || resourcePath.endsWith(".woff2") || // <--- QUI
                                resourcePath.endsWith(".ttf") || resourcePath.endsWith(".eot") ||    // <--- QUI
                                resourcePath.endsWith(".svg") || resourcePath.startsWith("media/")) {
                            return null;
                        }

                        // 3. CASO SPA (Single Page Application)
                        // Se arriviamo qui, è una rotta Angular (es. /cerca, /login) -> Servi index.html
                        return location.createRelative("index.html");
                    }
                });
    }
}