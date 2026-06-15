package br.org.assandef.assandefsystem.service;

import br.org.assandef.assandefsystem.model.DisponibilidadeSalao;
import br.org.assandef.assandefsystem.model.DisponibilidadeSalao.StatusDisponibilidadeSalao;
import br.org.assandef.assandefsystem.model.Funcionario;
import br.org.assandef.assandefsystem.model.SolicitacaoAluguelSalao;
import br.org.assandef.assandefsystem.model.SolicitacaoAluguelSalao.StatusSolicitacaoAluguelSalao;
import br.org.assandef.assandefsystem.repository.DisponibilidadeSalaoRepository;
import br.org.assandef.assandefsystem.repository.SolicitacaoAluguelSalaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitacaoAluguelSalaoService {

    private final SolicitacaoAluguelSalaoRepository solicitacaoAluguelSalaoRepository;
    private final DisponibilidadeSalaoRepository disponibilidadeSalaoRepository;
    private final DisponibilidadeSalaoService disponibilidadeSalaoService;

    public List<SolicitacaoAluguelSalao> findAll() {
        return solicitacaoAluguelSalaoRepository.findAllByOrderByDataSolicitacaoDesc();
    }

    public List<SolicitacaoAluguelSalao> findPendentes() {
        return solicitacaoAluguelSalaoRepository.findByStatusOrderByDataSolicitacaoDesc(
                StatusSolicitacaoAluguelSalao.PENDENTE
        );
    }

    public SolicitacaoAluguelSalao findById(Integer id) {
        return solicitacaoAluguelSalaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitação de aluguel não encontrada com ID: " + id));
    }

    @Transactional
    public SolicitacaoAluguelSalao criarSolicitacaoPublica(Integer idDisponibilidade, SolicitacaoAluguelSalao solicitacao) {
        DisponibilidadeSalao disponibilidade = disponibilidadeSalaoService.findById(idDisponibilidade);

        if (disponibilidade.getStatus() != StatusDisponibilidadeSalao.DISPONIVEL) {
            throw new RuntimeException("A data e horário selecionados não estão mais disponíveis.");
        }

        solicitacao.setIdSolicitacao(null);
        solicitacao.setDisponibilidade(disponibilidade);
        solicitacao.setValorApresentado(disponibilidade.getValor());
        solicitacao.setStatus(StatusSolicitacaoAluguelSalao.PENDENTE);
        solicitacao.setDataAnalise(null);
        solicitacao.setFuncionarioResponsavel(null);
        solicitacao.setObservacaoSecretaria(null);

        SolicitacaoAluguelSalao solicitacaoSalva = solicitacaoAluguelSalaoRepository.save(solicitacao);

        disponibilidade.setStatus(StatusDisponibilidadeSalao.EM_ANALISE);
        disponibilidadeSalaoRepository.save(disponibilidade);

        return solicitacaoSalva;
    }

    @Transactional
    public SolicitacaoAluguelSalao atualizarStatus(
            Integer idSolicitacao,
            StatusSolicitacaoAluguelSalao novoStatus,
            String observacaoSecretaria,
            Funcionario funcionarioResponsavel) {

        SolicitacaoAluguelSalao solicitacao = findById(idSolicitacao);
        solicitacao.setStatus(novoStatus);
        solicitacao.setObservacaoSecretaria(observacaoSecretaria);
        solicitacao.setFuncionarioResponsavel(funcionarioResponsavel);
        solicitacao.setDataAnalise(LocalDateTime.now());

        DisponibilidadeSalao disponibilidade = solicitacao.getDisponibilidade();

        if (novoStatus == StatusSolicitacaoAluguelSalao.APROVADA) {
            disponibilidade.setStatus(StatusDisponibilidadeSalao.RESERVADO);
        } else if (novoStatus == StatusSolicitacaoAluguelSalao.RECUSADA
                || novoStatus == StatusSolicitacaoAluguelSalao.CANCELADA) {
            disponibilidade.setStatus(StatusDisponibilidadeSalao.DISPONIVEL);
        } else if (novoStatus == StatusSolicitacaoAluguelSalao.EM_CONTATO
                || novoStatus == StatusSolicitacaoAluguelSalao.PENDENTE) {
            disponibilidade.setStatus(StatusDisponibilidadeSalao.EM_ANALISE);
        }

        disponibilidadeSalaoRepository.save(disponibilidade);
        return solicitacaoAluguelSalaoRepository.save(solicitacao);
    }

    @Transactional
    public void deleteById(Integer id) {
        if (!solicitacaoAluguelSalaoRepository.existsById(id)) {
            throw new RuntimeException("Solicitação de aluguel não encontrada com ID: " + id);
        }
        solicitacaoAluguelSalaoRepository.deleteById(id);
    }
}
