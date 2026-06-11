package br.org.assandef.assandefsystem.service;

import br.org.assandef.assandefsystem.model.Funcionario;
import br.org.assandef.assandefsystem.model.PasswordResetToken;
import br.org.assandef.assandefsystem.repository.FuncionarioRepository;
import br.org.assandef.assandefsystem.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final FuncionarioRepository funcionarioRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.security.password-reset.expiration-minutes}")
    private long expirationMinutes;

    @Value("${app.security.password-reset.frontend-url}")
    private String resetPageBaseUrl;

    @Transactional
    public void requestPasswordReset(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        var funcionarioOpt = funcionarioRepository.findByEmailIgnoreCase(email.trim());
        if (funcionarioOpt.isEmpty()) {
            return;
        }

        Funcionario funcionario = funcionarioOpt.get();
        passwordResetTokenRepository.deleteByFuncionario_IdFuncionario(funcionario.getIdFuncionario());

        String rawToken = generateSecureToken();
        String tokenHash = sha256(rawToken);

        PasswordResetToken token = new PasswordResetToken();
        token.setFuncionario(funcionario);
        token.setTokenHash(tokenHash);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(expirationMinutes));
        passwordResetTokenRepository.save(token);

        String link = buildResetLink(rawToken);
        emailService.sendPasswordResetEmail(funcionario.getEmail(), link, expirationMinutes);
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Token inválido");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("A nova senha deve ter no mínimo 8 caracteres");
        }

        String tokenHash = sha256(rawToken);
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido ou expirado"));

        if (token.getUsedAt() != null) {
            throw new IllegalArgumentException("Token inválido ou expirado");
        }
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token inválido ou expirado");
        }

        Funcionario funcionario = token.getFuncionario();
        funcionario.setSenhaHash(passwordEncoder.encode(newPassword));
        funcionarioRepository.save(funcionario);

        token.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(token);
    }

    private String buildResetLink(String rawToken) {
        String separator = resetPageBaseUrl.contains("?") ? "&" : "?";
        return resetPageBaseUrl + separator + "token=" + rawToken;
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao processar token", e);
        }
    }
}
