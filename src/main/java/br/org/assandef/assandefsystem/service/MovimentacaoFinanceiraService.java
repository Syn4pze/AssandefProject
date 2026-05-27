// =========================================================
// MovimentacaoFinanceiraService.java
// =========================================================
package br.org.assandef.assandefsystem.service;

import br.org.assandef.assandefsystem.model.ContaBancaria;
import br.org.assandef.assandefsystem.model.MovimentacaoFinanceira;
import br.org.assandef.assandefsystem.repository.ContaBancariaRepository;
import br.org.assandef.assandefsystem.repository.MovimentacaoFinanceiraRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimentacaoFinanceiraService {

    private final MovimentacaoFinanceiraRepository movimentacaoFinanceiraRepository;
    private final ContaBancariaRepository contaBancariaRepository; // ← injetado aqui

    public List<MovimentacaoFinanceira> findAll() {
        return movimentacaoFinanceiraRepository.findAll();
    }

    public MovimentacaoFinanceira findById(Integer id) {
        return movimentacaoFinanceiraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movimentação não encontrada com ID: " + id));
    }

    public void deleteById(Integer id) {
        if (!movimentacaoFinanceiraRepository.existsById(id)) {
            throw new RuntimeException("Movimentação não encontrada com ID: " + id);
        }
        movimentacaoFinanceiraRepository.deleteById(id);
    }

    @Transactional
    public MovimentacaoFinanceira save(MovimentacaoFinanceira movimentacao) {
        MovimentacaoFinanceira salva = movimentacaoFinanceiraRepository.save(movimentacao); // ← corrigido

        ContaBancaria conta = movimentacao.getConta();
        if (conta == null) {
            throw new RuntimeException("Conta bancária não informada na movimentação");
        }

        // Recarrega do banco para garantir saldo atualizado
        conta = contaBancariaRepository.findById(conta.getIdConta())
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));

        // Trata saldo null como zero
        BigDecimal saldoAtual = conta.getSaldo() != null ? conta.getSaldo() : BigDecimal.ZERO;

        if (movimentacao.getTipoMovimentacao() == MovimentacaoFinanceira.TipoMovimentacao.ENTRADA) {
            conta.setSaldo(saldoAtual.add(movimentacao.getValor()));

        } else if (movimentacao.getTipoMovimentacao() == MovimentacaoFinanceira.TipoMovimentacao.SAIDA) {
            // Impede saldo negativo
            if (movimentacao.getValor().compareTo(saldoAtual) > 0) {
                throw new RuntimeException("Saldo insuficiente! Saldo atual: R$ " + saldoAtual
                        + " | Valor da saída: R$ " + movimentacao.getValor());
            }
            conta.setSaldo(saldoAtual.subtract(movimentacao.getValor()));
        }

        contaBancariaRepository.save(conta);
        return salva;
    }
}