package br.org.assandef.assandefsystem.service;

import br.org.assandef.assandefsystem.config.PublicacoesStorageConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadService {

    private final PublicacoesStorageConfig storageConfig;

    @Value("${app.upload.max-tamanho-bytes}")
    private long maxTamanhoBytes;

    /**
     * Salva a imagem em uploads/publicacoes/imagens/{idPublicacao}/uuid.ext
     * Retorna o caminho relativo para gravar no banco.
     */
    public String salvarImagem(MultipartFile arquivo, Integer idPublicacao) throws IOException {
        validarArquivo(arquivo);
        validarTipoImagem(arquivo);

        // Subdiretório por publicação
        Path dir = storageConfig.getDiretorioImagens().resolve(String.valueOf(idPublicacao));
        Files.createDirectories(dir);

        String nomeArquivo = UUID.randomUUID() + extrairExtensao(arquivo.getOriginalFilename());
        arquivo.transferTo(dir.resolve(nomeArquivo));

        return "uploads/publicacoes/imagens/" + idPublicacao + "/" + nomeArquivo;
    }

    /**
     * Remove o arquivo físico do disco.
     */
    public void excluirArquivo(String caminhoRelativo) throws IOException {
        Files.deleteIfExists(Path.of(caminhoRelativo));
    }

    // ── Validações ────────────────────────────────────────────

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new RuntimeException("Arquivo inválido ou vazio.");
        }
        if (arquivo.getSize() > maxTamanhoBytes) {
            throw new RuntimeException("Arquivo excede o limite de 2 MB.");
        }
    }

    private void validarTipoImagem(MultipartFile arquivo) {
        String mime = arquivo.getContentType();
        if (mime == null || (!mime.equals("image/jpeg")
                && !mime.equals("image/png")
                && !mime.equals("image/webp"))) {
            throw new RuntimeException("Tipo de arquivo não permitido. Use JPEG, PNG ou WebP.");
        }
    }

    private String extrairExtensao(String nomeOriginal) {
        if (nomeOriginal != null && nomeOriginal.contains(".")) {
            return nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
        }
        return ".jpg";
    }
}