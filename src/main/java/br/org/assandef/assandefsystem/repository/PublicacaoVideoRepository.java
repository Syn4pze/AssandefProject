package br.org.assandef.assandefsystem.repository;

import br.org.assandef.assandefsystem.model.PublicacaoVideo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublicacaoVideoRepository extends JpaRepository<PublicacaoVideo, Integer> {

    List<PublicacaoVideo> findByPublicacaoIdPublicacaoOrderByOrdemExibicao(Integer idPublicacao);

    boolean existsByPublicacaoIdPublicacaoAndUrlYoutube(Integer idPublicacao, String urlYoutube);

    void deleteByPublicacaoIdPublicacao(Integer idPublicacao);
}