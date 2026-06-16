package br.org.assandef.assandefsystem.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${app.upload.dir:uploads/boletos}")
    private String uploadDir;

    /**
     * Salva o arquivo PDF e retorna o caminho relativo
     *
     * @param file Arquivo enviado pelo usuário
     * @param idDoador ID do doador (para organizar em pastas)
     * @param idBoleto ID do boleto (para nome único do arquivo)
     * @return Caminho relativo: "doador_1/2025-01/boleto_123.pdf"
     */
    public String salvarBoleto(MultipartFile file, Integer idDoador, Integer idBoleto) throws IOException {
        logger.info("Iniciando armazenamento de boleto para doador={} boleto={}", idDoador, idBoleto);

        if (file.isEmpty()) {
            throw new IOException("Arquivo vazio");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String contentType = file.getContentType();

        if (!originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new IOException("Apenas arquivos PDF são permitidos");
        }

        if (!"application/pdf".equalsIgnoreCase(contentType)) {
            throw new IOException("Tipo de arquivo inválido. Envie um PDF válido");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IOException("Arquivo muito grande. Máximo: 10MB");
        }

        String anoMes = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String subDiretorio = "doador_" + idDoador + "/" + anoMes;
        Path diretorioDestino = Paths.get(uploadDir, subDiretorio);

        Files.createDirectories(diretorioDestino);

        String nomeArquivo = "boleto_" + idBoleto + "_" + UUID.randomUUID().toString().substring(0, 8) + ".pdf";
        Path arquivoDestino = diretorioDestino.resolve(nomeArquivo);

        Files.copy(file.getInputStream(), arquivoDestino, StandardCopyOption.REPLACE_EXISTING);

        String caminhoRelativo = subDiretorio + "/" + nomeArquivo;
        logger.info("Boleto armazenado com sucesso em {}", caminhoRelativo);

        return caminhoRelativo;
    }

    /**
     * Deleta o arquivo do disco
     *
     * @param caminhoRelativo Caminho salvo no banco: "doador_1/2025-01/boleto_123.pdf"
     */
    public void deletarBoleto(String caminhoRelativo) throws IOException {
        if (caminhoRelativo == null || caminhoRelativo.isBlank()) {
            return; // Sem arquivo para deletar
        }

        Path arquivo = Paths.get(uploadDir, caminhoRelativo);

        if (Files.exists(arquivo)) {
            Files.delete(arquivo);
            logger.info("Arquivo de boleto removido: {}", arquivo);
        }
    }

    /**
     * Retorna o Path completo do arquivo
     *
     * @param caminhoRelativo Caminho salvo no banco
     * @return Path absoluto do arquivo
     */
    public Path obterArquivo(String caminhoRelativo) {
        return Paths.get(uploadDir, caminhoRelativo);
    }
}