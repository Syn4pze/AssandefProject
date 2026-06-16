package br.org.assandef.assandefsystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "disponibilidades_salao")
@Data
public class DisponibilidadeSalao {

    public enum StatusDisponibilidadeSalao {
        DISPONIVEL, EM_ANALISE, RESERVADO, BLOQUEADO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_disponibilidade")
    private Integer idDisponibilidade;

    @NotNull(message = "Data da locação é obrigatória")
    @Column(name = "data_locacao", nullable = false)
    private LocalDate dataLocacao;

    @NotNull(message = "Horário inicial é obrigatório")
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @NotNull(message = "Horário final é obrigatório")
    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.00", message = "O valor não pode ser negativo")
    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusDisponibilidadeSalao status = StatusDisponibilidadeSalao.DISPONIVEL;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @PrePersist
    public void prePersist() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
        if (status == null) {
            status = StatusDisponibilidadeSalao.DISPONIVEL;
        }
    }

    @PreUpdate
    public void preUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }
}
