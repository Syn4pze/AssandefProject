package br.org.assandef.assandefsystem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.org.assandef.assandefsystem.model.Funcionario;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Integer> {
    Optional<Funcionario> findByLogin(String login);
    Optional<Funcionario> findByEmailIgnoreCase(String email);
    List<Funcionario> findByNomeCompletoContainingIgnoreCase(String nome);
    List<Funcionario> findByHierarquia(Integer hierarquia);
}
