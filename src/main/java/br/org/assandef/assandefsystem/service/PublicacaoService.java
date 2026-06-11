package br.org.assandef.assandefsystem.service;

import br.org.assandef.assandefsystem.model.Publicacao;
import br.org.assandef.assandefsystem.model.Publicacao.TipoConteudo;
import br.org.assandef.assandefsystem.model.PublicacaoImagem;
import br.org.assandef.assandefsystem.model.PublicacaoVideo;
import br.org.assandef.assandefsystem.repository.PublicacaoImagemRepository;
import br.org.assandef.assandefsystem.repository.PublicacaoRepository;
import br.org.assandef.assandefsystem.repository.PublicacaoVideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
        return publicacaoRepository.findAll();
    }

    public Publicacao buscarPorId(Integer id) {
        return publicacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publicação não encontrada: " + id));
    }

    @Transactional
    public Publicacao salvar(Publicacao publicacao) {
        if (publicacao.getDataCriacao() == null) {
            publicacao.setDataCriacao(LocalDateTime.now());
        }
        return publicacaoRepository.save(publicacao);
    }

    @Transactional
    public Publicacao publicar(Integer id) {
        Publicacao pub = buscarPorId(id);
        pub.setDataPublicacao(LocalDateTime.now());
        return publicacaoRepository.save(pub);
    }

    @Transactional
    public Publicacao arquivar(Integer id) {
        Publicacao pub = buscarPorId(id);
        return publicacaoRepository.save(pub);
    }

    @Transactional
    public void excluir(Integer id) {
        buscarPorId(id); // valida existência
        publicacaoRepository.deleteById(id);
    }

    // ── Imagens ───────────────────────────────────────────────

    public List<PublicacaoImagem> listarImagensDaPublicacao(Integer idPublicacao) {
        return imagemRepository.findByPublicacaoIdPublicacaoOrderByOrdemExibicao(idPublicacao);
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

    // ── Vídeos ────────────────────────────────────────────────

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
}