package br.org.assandef.assandefsystem.controller;

import br.org.assandef.assandefsystem.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class PasswordRecoveryViewController {

    private final PasswordResetService passwordResetService;

    @GetMapping("/esqueci-senha")
    public String forgotPasswordPage() {
        return "acesso/forgot-password";
    }

    @PostMapping("/password/forgot")
    public String forgotPasswordSubmit(@RequestParam String email, RedirectAttributes ra) {
        passwordResetService.requestPasswordReset(email);
        ra.addFlashAttribute("msg", "Se existir uma conta com esse e-mail, as instruções foram enviadas.");
        return "redirect:/esqueci-senha";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage() {
        return "acesso/reset-password";
    }

    @PostMapping("/password/reset")
    public String resetPasswordSubmit(@RequestParam String token,
                                      @RequestParam String newPassword,
                                      @RequestParam String confirmPassword,
                                      RedirectAttributes ra) {
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("erro", "As senhas não coincidem.");
            return "redirect:/reset-password?token=" + token;
        }

        try {
            passwordResetService.resetPassword(token, newPassword);
            ra.addFlashAttribute("msg", "Senha redefinida com sucesso. Faça login com a nova senha.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/reset-password?token=" + token;
        }
    }
}
