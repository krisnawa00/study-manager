package lv.studymanager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Serve all static files from /static on the classpath.
     * Any path that doesn't match a real file falls back to index.html
     * so the PWA's client-side router takes over.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath,
                                                   Resource location) throws IOException {
                        Resource requested = location.createRelative(resourcePath);
                        // If the file exists, serve it directly
                        if (requested.exists() && requested.isReadable()) {
                            return requested;
                        }
                        // Otherwise fall back to index.html for SPA routing
                        return new ClassPathResource("/static/index.html");
                    }
                });
    }
}
