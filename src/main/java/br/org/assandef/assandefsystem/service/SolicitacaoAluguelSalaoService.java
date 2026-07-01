package br.org.assandef.assandefsystem.service;

import br.org.assandef.assandefsystem.model.Funcionario;
import br.org.assandef.assandefsystem.model.PlanoAluguelSalao;
import br.org.assandef.assandefsystem.model.SolicitacaoAluguelSalao;
import br.org.assandef.assandefsystem.model.SolicitacaoAluguelSalao.StatusSolicitacaoAluguelSalao;
import br.org.assandef.assandefsystem.repository.SolicitacaoAluguelSalaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitacaoAluguelSalaoService {

    private final SolicitacaoAluguelSalaoRepository solicitacaoAluguelSalaoRepository;
    private final PlanoAluguelSalaoService planoAluguelSalaoService;

    public List<SolicitacaoAluguelSalao> findAll() {
        return solicitacaoAluguelSalaoRepository.findAllByOrderByDataSolicitacaoDesc();
    }

    public List<SolicitacaoAluguelSalao> findPendentes() {
        return solicitacaoAluguelSalaoRepository.findByStatusOrderByDataSolicitacaoDesc(
                StatusSolicitacaoAluguelSalao.PENDENTE
        );
    }

    public List<SolicitacaoAluguelSalao> findAlugadas() {
        return solicitacaoAluguelSalaoRepository.findByStatusOrderByDataDesejadaAscHoraInicioDesejadaAsc(
                StatusSolicitacaoAluguelSalao.ALUGADO
        );
    }

    public List<String> findDatasOcupadasIso() {
        return solicitacaoAluguelSalaoRepository.findDatasByStatus(StatusSolicitacaoAluguelSalao.ALUGADO)
                .stream()
                .map(LocalDate::toString)
                .toList();
    }

    public SolicitacaoAluguelSalao findById(Integer id) {
        return solicitacaoAluguelSalaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitação de aluguel não encontrada com ID: " + id));
    }

    @Transactional
    public SolicitacaoAluguelSalao criarSolicitacaoPublica(SolicitacaoAluguelSalao solicitacao, Integer idPlanoAluguel) {
        validarSolicitacaoPublica(solicitacao);

        if (idPlanoAluguel == null) {
            throw new RuntimeException("Selecione um plano de aluguel.");
        }

        PlanoAluguelSalao plano = planoAluguelSalaoService.findById(idPlanoAluguel);
        if (!Boolean.TRUE.equals(plano.getAtivo())) {
            throw new RuntimeException("O plano selecionado não está disponível para novas solicitações.");
        }

        solicitacao.setIdSolicitacao(null);
        solicitacao.setPlanoAluguel(plano);
        solicitacao.setNomePlanoApresentado(plano.getNomePlano());
        solicitacao.setValorApresentado(plano.getValor());
        solicitacao.setStatus(StatusSolicitacaoAluguelSalao.PENDENTE);
        solicitacao.setDataAnalise(null);
        solicitacao.setFuncionarioResponsavel(null);
        solicitacao.setObservacaoSecretaria(null);

        return solicitacaoAluguelSalaoRepository.save(solicitacao);
    }

    @Transactional
    public SolicitacaoAluguelSalao atualizarStatus(
            Integer idSolicitacao,
            StatusSolicitacaoAluguelSalao novoStatus,
            String observacaoSecretaria,
            Funcionario funcionarioResponsavel) {

        SolicitacaoAluguelSalao solicitacao = findById(idSolicitacao);

        if (novoStatus == StatusSolicitacaoAluguelSalao.ALUGADO) {
            boolean existeConflito = solicitacaoAluguelSalaoRepository.existsSolicitacaoAlugadaConflitante(
                    solicitacao.getIdSolicitacao(),
                    StatusSolicitacaoAluguelSalao.ALUGADO,
                    solicitacao.getDataDesejada(),
                    solicitacao.getHoraInicioDesejada(),
                    solicitacao.getHoraFimDesejada()
            );

            if (existeConflito) {
                throw new RuntimeException("Já existe uma solicitação marcada como alugada para essa data e faixa de horário.");
            }
        }

        solicitacao.setStatus(novoStatus);
        solicitacao.setObservacaoSecretaria(observacaoSecretaria);
        solicitacao.setFuncionarioResponsavel(funcionarioResponsavel);
        solicitacao.setDataAnalise(LocalDateTime.now());

        return solicitacaoAluguelSalaoRepository.save(solicitacao);
    }

    @Transactional
    public void deleteById(Integer id) {
        if (!solicitacaoAluguelSalaoRepository.existsById(id)) {
            throw new RuntimeException("Solicitação de aluguel não encontrada com ID: " + id);
        }
        solicitacaoAluguelSalaoRepository.deleteById(id);
    }

    private void validarSolicitacaoPublica(SolicitacaoAluguelSalao solicitacao) {
        if (solicitacao.getDataDesejada() == null) {
            throw new RuntimeException("Informe a data desejada para o aluguel.");
        }

        if (solicitacao.getDataDesejada().isBefore(LocalDate.now())) {
            throw new RuntimeException("A data desejada não pode ser anterior à data atual.");
        }

        if (solicitacao.getHoraInicioDesejada() == null || solicitacao.getHoraFimDesejada() == null) {
            throw new RuntimeException("Informe o horário inicial e final desejado.");
        }

        if (!solicitacao.getHoraFimDesejada().isAfter(solicitacao.getHoraInicioDesejada())) {
            throw new RuntimeException("O horário final deve ser maior que o horário inicial.");
        }
    }
}
