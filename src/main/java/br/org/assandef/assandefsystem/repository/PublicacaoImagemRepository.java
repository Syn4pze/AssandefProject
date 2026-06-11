package br.org.assandef.assandefsystem.repository;

import br.org.assandef.assandefsystem.model.PublicacaoImagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublicacaoImagemRepository extends JpaRepository<PublicacaoImagem, Integer> {

    List<PublicacaoImagem> findByPublicacaoIdPublicacaoOrderByOrdemExibicao(Integer idPublicacao);

    long countByPublicacaoIdPublicacao(Integer idPublicacao);
}