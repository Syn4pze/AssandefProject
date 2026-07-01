package br.org.assandef.assandefsystem.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class AluguelSalaoStorageConfig implements WebMvcConfigurer {

    @Value("${app.upload.aluguel-salao.fotos.dir:uploads/aluguel-salao/fotos}")
    private String fotosDir;

    @PostConstruct
    public void init() {
        try {
            Path fotosPath = Paths.get(fotosDir);
            if (!Files.exists(fotosPath)) {
                Files.createDirectories(fotosPath);
                System.out.println("✅ Pasta de fotos do salão criada: " + fotosPath.toAbsolutePath());
            } else {
                System.out.println("✅ Pasta de fotos do salão já existe: " + fotosPath.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao criar diretório de fotos do salão: " + e.getMessage(), e);
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String fotosAbsoluto = Paths.get(fotosDir).toAbsolutePath().toString();

        registry.addResourceHandler("/uploads/aluguel-salao/fotos/**")
                .addResourceLocations("file:" + fotosAbsoluto + "/");
    }

    public Path getDiretorioFotos() {
        return Paths.get(fotosDir);
    }
}
