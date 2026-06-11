package br.org.assandef.assandefsystem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.security.password-reset.mail-from}")
    private String from;

    public void sendPasswordResetEmail(String to, String resetLink, long expirationMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Recuperação de senha - ASSANDEF");
        message.setText("Recebemos uma solicitação para redefinir sua senha.\n\n"
                + "Clique no link abaixo para continuar:\n"
                + resetLink + "\n\n"
                + "Este link expira em " + expirationMinutes + " minutos.\n"
                + "Se você não solicitou a redefinição, ignore este e-mail.");
        mailSender.send(message);
    }
}
