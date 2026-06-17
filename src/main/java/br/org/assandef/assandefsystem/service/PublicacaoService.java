package br.org.assandef.assandefsystem.service;

import br.org.assandef.assandefsystem.model.Publicacao;
import br.org.assandef.assandefsystem.model.PublicacaoImagem;
import br.org.assandef.assandefsystem.model.PublicacaoVideo;
import br.org.assandef.assandefsystem.repository.PublicacaoImagemRepository;
import br.org.assandef.assandefsystem.repository.PublicacaoRepository;
import br.org.assandef.assandefsystem.repository.PublicacaoVideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicacaoService {

    private final PublicacaoRepository publicacaoRepository;
    private final PublicacaoImagemRepository imagemRepository;
    private final PublicacaoVideoRepository videoRepository;

    public List<Publicacao> listarPorFuncionario(Integer idFuncionario) {
        return publicacaoRepository.findByFuncionarioAutorIdFuncionarioOrderByDataCriacaoDesc(idFuncionario);
    }

    public List<Publicacao> listarTodas() {
        return publicacaoRepository.findAllByOrderByDataCriacaoDesc();
    }

    public List<Publicacao> listarPublicadas() {
        return publicacaoRepository.findByDataPublicacaoIsNotNullOrderByDataPublicacaoDesc();
    }

    public Publicacao buscarPorId(Integer id) {
        return publicacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publicação não encontrada: " + id));
    }

    public Publicacao buscarPublicadaPorId(Integer id) {
        return publicacaoRepository.findByIdPublicacaoAndDataPublicacaoIsNotNull(id)
                .orElseThrow(() -> new RuntimeException("Publicação não encontrada ou ainda não publicada: " + id));
    }

    @Transactional
    public Publicacao salvar(Publicacao publicacao) {
        if (publicacao.getDataCriacao() == null) {
            publicacao.setDataCriacao(LocalDateTime.now());
        }
        publicacao.setDataAtualizacao(LocalDateTime.now());
        return publicacaoRepository.save(publicacao);
    }

    @Transactional
    public Publicacao publicar(Integer id) {
        Publicacao pub = buscarPorId(id);
        pub.setDataPublicacao(LocalDateTime.now());
        pub.setDataAtualizacao(LocalDateTime.now());
        return publicacaoRepository.saveAndFlush(pub);
    }

    @Transactional
    public Publicacao arquivar(Integer id) {
        Publicacao pub = buscarPorId(id);
        pub.setDataPublicacao(null);
        pub.setDataAtualizacao(LocalDateTime.now());
        return publicacaoRepository.save(pub);
    }

    @Transactional
    public void excluir(Integer id) {
        buscarPorId(id);
        publicacaoRepository.deleteById(id);
    }

    public List<PublicacaoImagem> listarImagensDaPublicacao(Integer idPublicacao) {
        return imagemRepository.findByPublicacaoIdPublicacaoOrderByOrdemExibicao(idPublicacao);
    }

    public Map<Integer, List<String>> mapearImagensPorPublicacao(List<Publicacao> publicacoes) {
        if (publicacoes == null || publicacoes.isEmpty()) {
            return Collections.emptyMap();
        }

        return publicacoes.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Publicacao::getIdPublicacao,
                        publicacao -> listarImagensDaPublicacao(publicacao.getIdPublicacao()).stream()
                                .map(PublicacaoImagem::getCaminhoArquivo)
                                .toList(),
                        (listaAtual, listaNova) -> listaAtual,
                        LinkedHashMap::new
                ));
    }

    @Transactional
    public PublicacaoImagem salvarImagem(PublicacaoImagem imagem) {
        long total = imagemRepository.countByPublicacaoIdPublicacao(
                imagem.getPublicacao().getIdPublicacao());
        if (total >= 10) {
            throw new RuntimeException("Limite de 10 imagens por publicação atingido.");
        }
        if (imagem.getDataUpload() == null) {
            imagem.setDataUpload(LocalDateTime.now());
        }
        return imagemRepository.save(imagem);
    }

    @Transactional
    public void excluirImagem(Integer idImagem) {
        imagemRepository.deleteById(idImagem);
    }

    public List<PublicacaoVideo> listarVideosDaPublicacao(Integer idPublicacao) {
        return videoRepository.findByPublicacaoIdPublicacaoOrderByOrdemExibicao(idPublicacao);
    }

    @Transactional
    public PublicacaoVideo salvarVideo(PublicacaoVideo video) {
        boolean duplicado = videoRepository.existsByPublicacaoIdPublicacaoAndUrlYoutube(
                video.getPublicacao().getIdPublicacao(), video.getUrlYoutube());
        if (duplicado) {
            throw new RuntimeException("Este vídeo já está vinculado a esta publicação.");
        }
        if (video.getDataCadastro() == null) {
            video.setDataCadastro(LocalDateTime.now());
        }
        return videoRepository.save(video);
    }

    @Transactional
    public void excluirVideo(Integer idVideo) {
        videoRepository.deleteById(idVideo);
    }

    @Transactional
    public void excluirVideosDaPublicacao(Integer idPublicacao) {
        videoRepository.deleteByPublicacaoIdPublicacao(idPublicacao);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> montarDadosJson(Integer id) {
        return montarDadosJson(buscarPorId(id));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> montarDadosPublicoJson(Integer id) {
        return montarDadosJson(buscarPublicadaPorId(id));
    }

    private Map<String, Object> montarDadosJson(Publicacao pub) {
        Integer id = pub.getIdPublicacao();
        List<String> imagens = listarImagensDaPublicacao(id).stream()
                .map(PublicacaoImagem::getCaminhoArquivo)
                .toList();
        List<String> videos = listarVideosDaPublicacao(id).stream()
                .map(PublicacaoVideo::getUrlYoutube)
                .toList();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", pub.getIdPublicacao());
        map.put("titulo", pub.getTitulo());
        map.put("descricao", pub.getDescricao());
        map.put("conteudo", pub.getConteudo());
        map.put("tipo", pub.getTipoConteudo() != null ? pub.getTipoConteudo().name() : "");
        map.put("data", pub.getDataCriacao() != null
                ? pub.getDataCriacao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH'h'mm"))
                : "");
        map.put("imagens", imagens);
        map.put("videos", videos);
        map.put("localEvento", pub.getLocalEvento());
        map.put("dataEvento", pub.getDataEvento() != null
                ? pub.getDataEvento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH'h'mm"))
                : null);
        map.put("dataEventoIso", pub.getDataEvento() != null ? pub.getDataEvento().toString() : null);
        map.put("publicada", pub.getDataPublicacao() != null);
        return map;
    }
}
