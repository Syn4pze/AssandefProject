package br.org.assandef.assandefsystem.repository;

import br.org.assandef.assandefsystem.model.Atendimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AtendimentoRepository extends JpaRepository<Atendimento, Integer> {
    List<Atendimento> findByPaciente_IdPaciente(Integer idPaciente);
    List<Atendimento> findByFuncionario_IdFuncionario(Integer idFuncionario);
    List<Atendimento> findByStatus(String status);
    List<Atendimento> findByDataHoraInicioBetween(LocalDateTime inicio, LocalDateTime fim);
    boolean existsByPaciente_IdPacienteAndStatus(Integer idPaciente, String status);
}