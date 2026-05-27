package br.org.assandef.assandefsystem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "funcionarios")
@Data
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_funcionario")
    private Integer idFuncionario   ;

    @NotBlank(message = "Nome completo é obrigatório")
    @Column(name = "nome_completo", nullable = false)
    private String nomeCompleto;

    @NotBlank(message = "Login é obrigatório")
    @Column(name = "login", nullable = false, unique = true, length = 80)
    private String login;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @NotNull(message = "Hierarquia é obrigatória")
    @Column(name = "hierarquia", nullable = false)
    private Integer hierarquia;

    @NotNull(message = "Email é obrigatório")
    @Column(name = "email", nullable = false)
    private String email;

}