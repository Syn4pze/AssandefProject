// =========================================================
// MovimentacaoFinanceiraRepository.java
// =========================================================
package br.org.assandef.assandefsystem.repository;

import br.org.assandef.assandefsystem.model.MovimentacaoFinanceira;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimentacaoFinanceiraRepository extends JpaRepository<MovimentacaoFinanceira, Integer> {

    List<MovimentacaoFinanceira> findAllByOrderByDataMovimentacaoDescIdMovimentacaoDesc();

    List<MovimentacaoFinanceira> findByConta_IdContaInOrderByDataMovimentacaoDescIdMovimentacaoDesc(List<Integer> idsContas);
}