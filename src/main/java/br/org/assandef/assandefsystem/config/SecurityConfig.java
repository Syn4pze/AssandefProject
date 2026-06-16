package br.org.assandef.assandefsystem.config;

import br.org.assandef.assandefsystem.security.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ApplicationContext applicationContext;
    @Value("${security.rememberme.key}")
    private String rememberMeKey;

    public SecurityConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/api/**",
                                "/doadores/newdonation",
                                "/doadores/newdonation/",
                                "/doadores/salvar",
                                "/doadores/salvar/",
                                "/aluguel-salao/solicitar",
                                "/aluguel-salao/solicitar/"
                        )
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation(sessionFixation -> sessionFixation.migrateSession())
                        .maximumSessions(2)
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                        .contentTypeOptions(contentType -> {
                        })
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/webjars/**").permitAll()
                        .requestMatchers(
                                "/",
                                "/login",
                                "/esqueci-senha",
                                "/reset-password",
                                "/doadores/newdonation",
                                "/doadores/newdonation/",
                                "/sobre",
                                "/publicacoes",
                                "/publicacoes/**",
                                "/aluguel-salao",
                                "/aluguel-salao/",
                                "/images/**",
                                "/uploads/**",
                                "/error",
                                "/error/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/doadores/salvar",
                                "/doadores/salvar/",
                                "/password/forgot",
                                "/password/reset",
                                "/aluguel-salao/solicitar",
                                "/aluguel-salao/solicitar/"
                        ).permitAll()

                        // /aluguel-salao/gestao/** -> hierarquia 1 ou 2
                        .requestMatchers("/aluguel-salao/gestao", "/aluguel-salao/gestao/**")
                        .access((authSupplier, ctx) -> {
                            var authentication = authSupplier.get();
                            AuthService authService = applicationContext.getBean(AuthService.class);
                            boolean allowed = authService.hasAnyHierarquia(authentication, 1, 2);
                            return new AuthorizationDecision(allowed);
                        })

                        // /funcionarios/** -> somente hierarquia 1
                        .requestMatchers("/funcionarios/**")
                        .access((authSupplier, ctx) -> {
                            var authentication = authSupplier.get();
                            AuthService authService = applicationContext.getBean(AuthService.class);
                            boolean allowed = authService.hasHierarquia(authentication, 1);
                            return new AuthorizationDecision(allowed);
                        })

                        // /almoxarifado/** -> hierarquia 1 ou 3
                        .requestMatchers("/almoxarifado/**")
                        .access((authSupplier, ctx) -> {
                            var authentication = authSupplier.get();
                            AuthService authService = applicationContext.getBean(AuthService.class);
                            boolean allowed = authService.hasAnyHierarquia(authentication, 1,2,3);
                            return new AuthorizationDecision(allowed);
                        })

                        // /atendimento/** -> hierarquia 1 ou 2
                        .requestMatchers("/atendimento/**")
                        .access((authSupplier, ctx) -> {
                            var authentication = authSupplier.get();
                            AuthService authService = applicationContext.getBean(AuthService.class);
                            boolean allowed = authService.hasAnyHierarquia(authentication, 1, 2);
                            return new AuthorizationDecision(allowed);
                        })

                        // /pacientes/** -> hierarquia 1 ou 2
                        .requestMatchers("/pacientes/**")
                        .access((authSupplier, ctx) -> {
                            var authentication = authSupplier.get();
                            AuthService authService = applicationContext.getBean(AuthService.class);
                            boolean allowed = authService.hasAnyHierarquia(authentication, 1, 2);
                            return new AuthorizationDecision(allowed);
                        })

                        .requestMatchers(HttpMethod.GET, "/doadores/editar/**")
                        .access((authSupplier, ctx) -> {
                            var authentication = authSupplier.get();
                            AuthService authService = applicationContext.getBean(AuthService.class);
                            boolean allowed = authService.hasAnyHierarquia(authentication, 1, 3);
                            return new AuthorizationDecision(allowed);
                        })
                        .requestMatchers(HttpMethod.POST, "/doadores/editar/**")
                        .access((authSupplier, ctx) -> {
                            var authentication = authSupplier.get();
                            AuthService authService = applicationContext.getBean(AuthService.class);
                            boolean allowed = authService.hasAnyHierarquia(authentication, 1, 3);
                            return new AuthorizationDecision(allowed);
                        })
                        .requestMatchers(HttpMethod.GET, "/doadores/deletar/**")
                        .access((authSupplier, ctx) -> {
                            var authentication = authSupplier.get();
                            AuthService authService = applicationContext.getBean(AuthService.class);
                            boolean allowed = authService.hasAnyHierarquia(authentication, 1, 3);
                            return new AuthorizationDecision(allowed);
                        })

                        // /doadores/** -> hierarquia 1 ou 3
                        .requestMatchers("/doadores/**")
                        .access((authSupplier, ctx) -> {
                            var authentication = authSupplier.get();
                            AuthService authService = applicationContext.getBean(AuthService.class);
                            boolean allowed = authService.hasAnyHierarquia(authentication, 1, 3);
                            return new AuthorizationDecision(allowed);
                        })

                        // /doadores/** -> hierarquia 1 ou 3
                        .requestMatchers("/funcionarios/**")
                        .access((authSupplier, ctx) -> {
                            var authentication = authSupplier.get();
                            AuthService authService = applicationContext.getBean(AuthService.class);
                            boolean allowed = authService.hasAnyHierarquia(authentication, 1);
                            return new AuthorizationDecision(allowed);
                        })
                        // /financeiro/** -> hierarquia 1 (Diretoria) ou 3 (Administrativo)
                        .requestMatchers("/financeiro/**")
                        .access((authSupplier, ctx) -> {
                            var authentication = authSupplier.get();
                            AuthService authService = applicationContext.getBean(AuthService.class);
                            // Permitindo Diretoria (1) e Administrativo (3)
                            boolean allowed = authService.hasAnyHierarquia(authentication, 1, 3);
                            return new AuthorizationDecision(allowed);
                        })


                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .key(rememberMeKey)
                        .tokenValiditySeconds(86400)
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/error/403")
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}