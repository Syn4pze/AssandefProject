package br.org.assandef.assandefsystem.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "contas_bancarias")
@Data
public class ContaBancaria {
    public enum TipoConta {
        CORRENTE, POUPANCA, CAIXA
    }

    public enum StatusConta {
        ATIVA, INATIVA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conta")
    private Integer idConta;

    @Column(name = "nome_banco", length = 100)
    private String nomeBanco;

    @Column(name = "agencia", length = 20)
    private String agencia;

    @Column(name = "numero_conta", length = 30)
    private String numeroConta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_conta")
    private TipoConta tipoConta; // Ex: CORRENTE, POUPANCA, CAIXA

    @Column(name = "saldo", precision = 10, scale = 2)
    private BigDecimal saldo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StatusConta status; // Ex: ATIVA, INATIVA

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;
}