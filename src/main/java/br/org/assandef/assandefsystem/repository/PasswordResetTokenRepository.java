package br.org.assandef.assandefsystem.repository;

import br.org.assandef.assandefsystem.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    void deleteByFuncionario_IdFuncionario(Integer idFuncionario);
}
