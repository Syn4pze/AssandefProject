package br.org.assandef.assandefsystem.repository;

import br.org.assandef.assandefsystem.model.DisponibilidadeSalao;
import br.org.assandef.assandefsystem.model.DisponibilidadeSalao.StatusDisponibilidadeSalao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DisponibilidadeSalaoRepository extends JpaRepository<DisponibilidadeSalao, Integer> {

    List<DisponibilidadeSalao> findAllByOrderByDataLocacaoAscHoraInicioAsc();

    List<DisponibilidadeSalao> findByStatusOrderByDataLocacaoAscHoraInicioAsc(StatusDisponibilidadeSalao status);

    List<DisponibilidadeSalao> findByStatusAndDataLocacaoGreaterThanEqualOrderByDataLocacaoAscHoraInicioAsc(
            StatusDisponibilidadeSalao status,
            LocalDate dataLocacao
    );
}
