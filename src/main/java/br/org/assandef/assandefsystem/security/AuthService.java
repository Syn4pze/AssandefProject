package br.org.assandef.assandefsystem.security;

import br.org.assandef.assandefsystem.model.Funcionario;
import br.org.assandef.assandefsystem.service.FuncionarioService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("authService")
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final FuncionarioService funcionarioService;

    public boolean hasHierarquia(Authentication authentication, int nivel) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.debug("Negando acesso por autenticação ausente ou inválida em hasHierarquia para nível {}", nivel);
                return false;
            }
            String username = authentication.getName();
            if (username == null) {
                logger.debug("Negando acesso por usuário nulo em hasHierarquia para nível {}", nivel);
                return false;
            }
            Funcionario f = funcionarioService.findByLogin(username);
            if (f == null || f.getHierarquia() == null) {
                logger.debug("Negando acesso para usuário {} por hierarquia ausente", username);
                return false;
            }
            boolean permitido = f.getHierarquia().intValue() == nivel;
            if (!permitido) {
                logger.debug("Negando acesso para usuário {}: hierarquia {} != nível exigido {}", username, f.getHierarquia(), nivel);
            }
            return permitido;
        } catch (Exception ex) {
            logger.warn("Erro ao verificar hierarquia para nível {}", nivel, ex);
            return false;
        }
    }

    public boolean hasAnyHierarquia(Authentication authentication, Integer... niveis) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.debug("Negando acesso por autenticação ausente ou inválida em hasAnyHierarquia");
                return false;
            }
            String username = authentication.getName();
            if (username == null) {
                logger.debug("Negando acesso por usuário nulo em hasAnyHierarquia");
                return false;
            }
            Funcionario f = funcionarioService.findByLogin(username);
            if (f == null || f.getHierarquia() == null) {
                logger.debug("Negando acesso para usuário {} por hierarquia ausente", username);
                return false;
            }
            for (Integer n : niveis) {
                if (f.getHierarquia().equals(n)) return true;
            }
            logger.debug("Negando acesso para usuário {}: hierarquia {} não está entre os níveis permitidos", username, f.getHierarquia());
        } catch (Exception ex) {
            logger.warn("Erro ao verificar múltiplas hierarquias", ex);
            return false;
        }
        return false;
    }
}