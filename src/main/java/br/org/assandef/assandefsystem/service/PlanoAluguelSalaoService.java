package br.org.assandef.assandefsystem.service;

import br.org.assandef.assandefsystem.model.PlanoAluguelSalao;
import br.org.assandef.assandefsystem.repository.PlanoAluguelSalaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanoAluguelSalaoService {

    private final PlanoAluguelSalaoRepository planoAluguelSalaoRepository;

    public List<PlanoAluguelSalao> findAll() {
        return planoAluguelSalaoRepository.findAllByOrderByAtivoDescValorAscNomePlanoAsc();
    }

    public List<PlanoAluguelSalao> findAtivos() {
        return planoAluguelSalaoRepository.findByAtivoTrueOrderByValorAscNomePlanoAsc();
    }

    public PlanoAluguelSalao findById(Integer idPlano) {
        return planoAluguelSalaoRepository.findById(idPlano)
                .orElseThrow(() -> new RuntimeException("Plano de aluguel não encontrado com ID: " + idPlano));
    }

    @Transactional
    public PlanoAluguelSalao salvar(Integer idPlano,
                                    String nomePlano,
                                    BigDecimal valor,
                                    String itensInclusos,
                                    String descricao,
                                    boolean ativo) {
        if (nomePlano == null || nomePlano.isBlank()) {
            throw new RuntimeException("Informe o nome do plano.");
        }
        if (valor == null || valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Informe um valor válido para o plano.");
        }
        if (itensInclusos == null || itensInclusos.isBlank()) {
            throw new RuntimeException("Informe o que está incluído no plano.");
        }

        PlanoAluguelSalao plano = idPlano == null ? new PlanoAluguelSalao() : findById(idPlano);
        plano.setNomePlano(nomePlano.trim());
        plano.setValor(valor);
        plano.setItensInclusos(itensInclusos.trim());
        plano.setDescricao(descricao == null ? null : descricao.trim());
        plano.setAtivo(ativo);

        return planoAluguelSalaoRepository.save(plano);
    }

    @Transactional
    public void alterarStatus(Integer idPlano, boolean ativo) {
        PlanoAluguelSalao plano = findById(idPlano);
        plano.setAtivo(ativo);
        planoAluguelSalaoRepository.save(plano);
    }

    @Transactional
    public void deleteById(Integer idPlano) {
        if (!planoAluguelSalaoRepository.existsById(idPlano)) {
            throw new RuntimeException("Plano de aluguel não encontrado com ID: " + idPlano);
        }
        planoAluguelSalaoRepository.deleteById(idPlano);
    }
}
