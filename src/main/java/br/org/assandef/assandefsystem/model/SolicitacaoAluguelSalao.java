package br.org.assandef.assandefsystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "solicitacoes_aluguel_salao")
@Data
public class SolicitacaoAluguelSalao {

    public enum TipoDocumento {
        CPF, CNPJ
    }

    public enum StatusSolicitacaoAluguelSalao {
        PENDENTE, EM_CONTATO, ALUGADO, RECUSADA, CANCELADA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitacao")
    private Integer idSolicitacao;

    @NotBlank(message = "Nome do responsável é obrigatório")
    @Column(name = "nome_responsavel", nullable = false)
    private String nomeResponsavel;

    @NotNull(message = "Tipo de documento é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false)
    private TipoDocumento tipoDocumento;

    @NotBlank(message = "Documento é obrigatório")
    @Column(name = "documento", nullable = false, length = 18)
    private String documento;

    @NotBlank(message = "Celular é obrigatório")
    @Column(name = "celular", nullable = false, length = 20)
    private String celular;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "Informe um e-mail válido")
    @Column(name = "email", nullable = false)
    private String email;

    @NotNull(message = "Data desejada é obrigatória")
    @Column(name = "data_desejada", nullable = false)
    private LocalDate dataDesejada;

    @NotNull(message = "Hora inicial desejada é obrigatória")
    @Column(name = "hora_inicio_desejada", nullable = false)
    private LocalTime horaInicioDesejada;

    @NotNull(message = "Hora final desejada é obrigatória")
    @Column(name = "hora_fim_desejada", nullable = false)
    private LocalTime horaFimDesejada;

    @NotBlank(message = "Motivo do aluguel é obrigatório")
    @Column(name = "motivo_aluguel", nullable = false, columnDefinition = "TEXT")
    private String motivoAluguel;

    @Column(name = "valor_apresentado", precision = 10, scale = 2)
    private BigDecimal valorApresentado;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusSolicitacaoAluguelSalao status = StatusSolicitacaoAluguelSalao.PENDENTE;

    @Column(name = "observacao_secretaria", columnDefinition = "TEXT")
    private String observacaoSecretaria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_funcionario_responsavel")
    private Funcionario funcionarioResponsavel;

    @Column(name = "data_solicitacao", nullable = false, updatable = false)
    private LocalDateTime dataSolicitacao;

    @Column(name = "data_analise")
    private LocalDateTime dataAnalise;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @PrePersist
    public void prePersist() {
        if (dataSolicitacao == null) {
            dataSolicitacao = LocalDateTime.now();
        }
        if (status == null) {
            status = StatusSolicitacaoAluguelSalao.PENDENTE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }
}
