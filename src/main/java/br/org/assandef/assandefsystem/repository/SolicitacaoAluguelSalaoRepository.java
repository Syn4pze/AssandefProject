package br.org.assandef.assandefsystem.repository;

import br.org.assandef.assandefsystem.model.SolicitacaoAluguelSalao;
import br.org.assandef.assandefsystem.model.SolicitacaoAluguelSalao.StatusSolicitacaoAluguelSalao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitacaoAluguelSalaoRepository extends JpaRepository<SolicitacaoAluguelSalao, Integer> {

    List<SolicitacaoAluguelSalao> findAllByOrderByDataSolicitacaoDesc();

    List<SolicitacaoAluguelSalao> findByStatusOrderByDataSolicitacaoDesc(StatusSolicitacaoAluguelSalao status);
}
