package br.org.assandef.assandefsystem.repository;

import br.org.assandef.assandefsystem.model.SolicitacaoAluguelSalao;
import br.org.assandef.assandefsystem.model.SolicitacaoAluguelSalao.StatusSolicitacaoAluguelSalao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface SolicitacaoAluguelSalaoRepository extends JpaRepository<SolicitacaoAluguelSalao, Integer> {

    List<SolicitacaoAluguelSalao> findAllByOrderByDataSolicitacaoDesc();

    List<SolicitacaoAluguelSalao> findByStatusOrderByDataSolicitacaoDesc(StatusSolicitacaoAluguelSalao status);

    @Query("""
            select count(s) > 0
            from SolicitacaoAluguelSalao s
            where s.idSolicitacao <> :idSolicitacao
              and s.status = :status
              and s.dataDesejada = :dataDesejada
              and s.horaInicioDesejada < :horaFimDesejada
              and s.horaFimDesejada > :horaInicioDesejada
            """)
    boolean existsSolicitacaoAlugadaConflitante(
            @Param("idSolicitacao") Integer idSolicitacao,
            @Param("status") StatusSolicitacaoAluguelSalao status,
            @Param("dataDesejada") LocalDate dataDesejada,
            @Param("horaInicioDesejada") LocalTime horaInicioDesejada,
            @Param("horaFimDesejada") LocalTime horaFimDesejada
    );
}
