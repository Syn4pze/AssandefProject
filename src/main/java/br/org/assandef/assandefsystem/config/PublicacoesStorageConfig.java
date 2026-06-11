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
public class PublicacoesStorageConfig implements WebMvcConfigurer {

    @Value("${app.upload.publicacoes.imagens.dir:uploads/publicacoes/imagens}")
    private String imagensDir;

    /**
     * Cria o diretório de imagens automaticamente ao iniciar a aplicação.
     * O diretório de vídeos não é necessário pois apenas links YouTube são armazenados.
     */
    @PostConstruct
    public void init() {
        try {
            Path imagensPath = Paths.get(imagensDir);
            if (!Files.exists(imagensPath)) {
                Files.createDirectories(imagensPath);
                System.out.println("✅ Pasta de imagens de publicações criada: " + imagensPath.toAbsolutePath());
            } else {
                System.out.println("✅ Pasta de imagens de publicações já existe: " + imagensPath.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao criar diretório de imagens: " + e.getMessage(), e);
        }
        System.out.println("📁 Diretório de imagens (absoluto): " + Paths.get(imagensDir).toAbsolutePath());
        System.out.println("📁 Working directory: " + System.getProperty("user.dir"));
    }

    /**
     * Serve os arquivos de imagem estaticamente.
     * Exemplo: http://localhost:8080/uploads/publicacoes/imagens/1/uuid.jpg
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Usa o mesmo path absoluto que o UploadService usa para salvar
        String imagensAbsoluto = Paths.get(imagensDir).toAbsolutePath().toString();

        registry.addResourceHandler("/uploads/publicacoes/imagens/**")
                .addResourceLocations("file:" + imagensAbsoluto + "/");
    }

    public Path getDiretorioImagens() {
        return Paths.get(imagensDir);
    }
}