package br.org.assandef.assandefsystem.repository;

import br.org.assandef.assandefsystem.model.PlanoAluguelSalao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanoAluguelSalaoRepository extends JpaRepository<PlanoAluguelSalao, Integer> {

    List<PlanoAluguelSalao> findAllByOrderByAtivoDescValorAscNomePlanoAsc();

    List<PlanoAluguelSalao> findByAtivoTrueOrderByValorAscNomePlanoAsc();
}
