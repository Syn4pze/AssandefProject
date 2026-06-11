package br.org.assandef.assandefsystem.controller;

import br.org.assandef.assandefsystem.model.Funcionario;
import br.org.assandef.assandefsystem.security.JwtService;
import br.org.assandef.assandefsystem.service.FuncionarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final FuncionarioService funcionarioService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody JwtLoginRequest request) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            String username = authentication.getName();
            Funcionario funcionario = funcionarioService.findByLogin(username);
            String token = jwtService.generateToken(username, Map.of(
                    "hierarquia", funcionario.getHierarquia(),
                    "nome", funcionario.getNomeCompleto()
            ));

            return ResponseEntity.ok(new JwtLoginResponse(
                    token,
                    "Bearer",
                    jwtService.getExpirationMs(),
                    username,
                    funcionario.getHierarquia(),
                    funcionario.getNomeCompleto()
            ));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciais inválidas"));
        }
    }

    public record JwtLoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {
    }

    public record JwtLoginResponse(
            String accessToken,
            String tokenType,
            long expiresInMs,
            String username,
            Integer hierarquia,
            String nome
    ) {
    }
}
