package br.org.assandef.assandefsystem.config;

import br.org.assandef.assandefsystem.security.AuthService;
import br.org.assandef.assandefsystem.security.JwtAuthenticationEntryPoint;
import br.org.assandef.assandefsystem.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;

@Configuration
@RequiredArgsConstructor
public class ApiSecurityConfig {

    private final AuthService authService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .rememberMe(remember -> remember.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/password/forgot", "/api/auth/password/reset").permitAll()
                        .requestMatchers("/api/funcionarios/**")
                        .access((authSupplier, ctx) -> new AuthorizationDecision(
                                authService.hasHierarquia(authSupplier.get(), 1)
                        ))
                        .requestMatchers("/api/almoxarifado/**")
                        .access((authSupplier, ctx) -> new AuthorizationDecision(
                                authService.hasAnyHierarquia(authSupplier.get(), 1, 2, 3)
                        ))
                        .requestMatchers("/api/atendimento/**")
                        .access((authSupplier, ctx) -> new AuthorizationDecision(
                                authService.hasAnyHierarquia(authSupplier.get(), 1, 2)
                        ))
                        .requestMatchers("/api/pacientes/**")
                        .access((authSupplier, ctx) -> new AuthorizationDecision(
                                authService.hasAnyHierarquia(authSupplier.get(), 1, 2)
                        ))
                        .requestMatchers("/api/doadores/**")
                        .access((authSupplier, ctx) -> new AuthorizationDecision(
                                authService.hasAnyHierarquia(authSupplier.get(), 1, 3)
                        ))
                        .requestMatchers("/api/financeiro/**")
                        .access((authSupplier, ctx) -> new AuthorizationDecision(
                                authService.hasAnyHierarquia(authSupplier.get(), 1, 3)
                        ))
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                            response.getWriter().write("{\"error\":\"forbidden\",\"message\":\"Acesso negado\"}");
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
