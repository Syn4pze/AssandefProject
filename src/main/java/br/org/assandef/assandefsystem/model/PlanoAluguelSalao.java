package br.org.assandef.assandefsystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "planos_aluguel_salao")
@Data
public class PlanoAluguelSalao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plano")
    private Integer idPlano;

    @NotBlank(message = "Nome do plano é obrigatório")
    @Column(name = "nome_plano", nullable = false, length = 120)
    private String nomePlano;

    @NotNull(message = "Valor do plano é obrigatório")
    @DecimalMin(value = "0.00", message = "O valor não pode ser negativo")
    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @NotBlank(message = "Informe o que está incluído no plano")
    @Column(name = "itens_inclusos", nullable = false, columnDefinition = "TEXT")
    private String itensInclusos;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @PrePersist
    public void prePersist() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
        if (ativo == null) {
            ativo = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }
}
