package br.org.assandef.assandefsystem.service;

import br.org.assandef.assandefsystem.config.AluguelSalaoStorageConfig;
import br.org.assandef.assandefsystem.model.FotoSalao;
import br.org.assandef.assandefsystem.repository.FotoSalaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FotoSalaoService {

    private final FotoSalaoRepository fotoSalaoRepository;
    private final AluguelSalaoStorageConfig storageConfig;

    @Value("${app.upload.max-tamanho-bytes:2097152}")
    private long maxTamanhoBytes;

    public List<FotoSalao> findAll() {
        return fotoSalaoRepository.findAllByOrderByAtivoDescFotoPrincipalDescOrdemExibicaoAscDataUploadDesc();
    }

    public List<FotoSalao> findAtivas() {
        return fotoSalaoRepository.findByAtivoTrueOrderByFotoPrincipalDescOrdemExibicaoAscDataUploadDesc();
    }

    public FotoSalao findById(Integer idFoto) {
        return fotoSalaoRepository.findById(idFoto)
                .orElseThrow(() -> new RuntimeException("Foto do salão não encontrada com ID: " + idFoto));
    }

    @Transactional
    public FotoSalao salvar(Integer idFoto,
                            String titulo,
                            String descricao,
                            Integer ordemExibicao,
                            boolean fotoPrincipal,
                            boolean ativo,
                            MultipartFile arquivo) throws IOException {
        FotoSalao foto = idFoto == null ? new FotoSalao() : findById(idFoto);

        boolean novaFoto = foto.getIdFoto() == null;
        boolean arquivoFoiEnviado = arquivo != null && !arquivo.isEmpty();

        if (novaFoto && !arquivoFoiEnviado) {
            throw new RuntimeException("Selecione uma imagem para cadastrar a foto do salão.");
        }

        if (arquivoFoiEnviado) {
            validarArquivoImagem(arquivo);
            removerArquivoFisico(foto.getCaminhoArquivo());
            String caminho = salvarArquivoFisico(arquivo);
            foto.setCaminhoArquivo(caminho);
            foto.setNomeOriginal(StringUtils.cleanPath(arquivo.getOriginalFilename() == null ? "foto-salao" : arquivo.getOriginalFilename()));
            foto.setTipoMime(arquivo.getContentType());
            foto.setTamanhoBytes(arquivo.getSize());
        }

        foto.setTitulo(titulo == null ? null : titulo.trim());
        foto.setDescricao(descricao == null ? null : descricao.trim());
        foto.setOrdemExibicao(ordemExibicao == null ? 0 : ordemExibicao);
        foto.setFotoPrincipal(fotoPrincipal);
        foto.setAtivo(ativo);

        if (fotoPrincipal) {
            limparFotoPrincipal();
        }

        return fotoSalaoRepository.save(foto);
    }

    @Transactional
    public void definirPrincipal(Integer idFoto) {
        FotoSalao foto = findById(idFoto);
        limparFotoPrincipal();
        foto.setFotoPrincipal(true);
        foto.setAtivo(true);
        fotoSalaoRepository.save(foto);
    }

    @Transactional
    public void alterarStatus(Integer idFoto, boolean ativo) {
        FotoSalao foto = findById(idFoto);
        foto.setAtivo(ativo);
        fotoSalaoRepository.save(foto);
    }

    @Transactional
    public void deleteById(Integer idFoto) throws IOException {
        FotoSalao foto = findById(idFoto);
        removerArquivoFisico(foto.getCaminhoArquivo());
        fotoSalaoRepository.delete(foto);
    }

    private void limparFotoPrincipal() {
        List<FotoSalao> fotos = fotoSalaoRepository.findAll();
        for (FotoSalao foto : fotos) {
            if (Boolean.TRUE.equals(foto.getFotoPrincipal())) {
                foto.setFotoPrincipal(false);
            }
        }
        fotoSalaoRepository.saveAll(fotos);
    }

    private String salvarArquivoFisico(MultipartFile arquivo) throws IOException {
        Path dir = storageConfig.getDiretorioFotos();
        Files.createDirectories(dir);

        String nomeOriginal = StringUtils.cleanPath(arquivo.getOriginalFilename() == null ? "foto-salao" : arquivo.getOriginalFilename());
        String nomeArquivo = UUID.randomUUID() + extrairExtensao(nomeOriginal);
        Path destino = dir.resolve(nomeArquivo);

        arquivo.transferTo(destino);
        return "uploads/aluguel-salao/fotos/" + nomeArquivo;
    }

    private void removerArquivoFisico(String caminhoRelativo) throws IOException {
        if (caminhoRelativo == null || caminhoRelativo.isBlank()) {
            return;
        }
        Files.deleteIfExists(Path.of(caminhoRelativo));
    }

    private void validarArquivoImagem(MultipartFile arquivo) {
        if (arquivo.getSize() > maxTamanhoBytes) {
            throw new RuntimeException("Arquivo muito grande. O limite configurado é de " + (maxTamanhoBytes / 1024 / 1024) + " MB.");
        }

        String mime = arquivo.getContentType();
        if (mime == null || (!mime.equals("image/jpeg") && !mime.equals("image/png") && !mime.equals("image/webp"))) {
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
