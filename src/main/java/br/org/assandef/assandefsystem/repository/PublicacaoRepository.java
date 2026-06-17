package br.org.assandef.assandefsystem.repository;

import br.org.assandef.assandefsystem.model.Publicacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublicacaoRepository extends JpaRepository<Publicacao, Integer> {

    List<Publicacao> findByFuncionarioAutorIdFuncionarioOrderByDataCriacaoDesc(Integer idFuncionario);

    List<Publicacao> findAllByOrderByDataCriacaoDesc();

    List<Publicacao> findByDataPublicacaoIsNotNullOrderByDataPublicacaoDesc();
}
