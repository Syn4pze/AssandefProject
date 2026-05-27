package br.org.assandef.assandefsystem.model;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "movimentacoes_financeiras")
@Data
public class MovimentacaoFinanceira {
    public enum TipoMovimentacao {
        ENTRADA, SAIDA
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimentacao")
    private Integer idMovimentacao;

    @ManyToOne
    @JoinColumn(name = "id_conta", nullable = false)
    private ContaBancaria conta;

    @ManyToOne
    @JoinColumn(name = "id_categoria_financeira", nullable = false)
    private CategoriaFinanceira categoriaFinanceira;

    @ManyToOne
    @JoinColumn(name = "id_funcionario")
    private Funcionario funcionario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimentacao", nullable = false)
    private TipoMovimentacao tipoMovimentacao; // Ex: ENTRADA, SAIDA

    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_movimentacao", nullable = false)
    private LocalDate dataMovimentacao;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;
}