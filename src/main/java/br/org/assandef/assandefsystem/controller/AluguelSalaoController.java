package br.org.assandef.assandefsystem.controller;

import br.org.assandef.assandefsystem.model.Funcionario;
import br.org.assandef.assandefsystem.model.SolicitacaoAluguelSalao;
import br.org.assandef.assandefsystem.model.SolicitacaoAluguelSalao.StatusSolicitacaoAluguelSalao;
import br.org.assandef.assandefsystem.service.FuncionarioService;
import br.org.assandef.assandefsystem.service.SolicitacaoAluguelSalaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AluguelSalaoController {

    private final SolicitacaoAluguelSalaoService solicitacaoAluguelSalaoService;
    private final FuncionarioService funcionarioService;

    // =========================================================
    // PÁGINA PÚBLICA
    // =========================================================

    @GetMapping("/aluguel-salao")
    public String exibirPaginaPublica(Model model) {
        model.addAttribute("ativa", "aluguelSalao");
        model.addAttribute("solicitacaoForm", new SolicitacaoAluguelSalao());
        model.addAttribute("tiposDocumento", SolicitacaoAluguelSalao.TipoDocumento.values());
        return "aluguel-salao/aluguel-salao";
    }

    @PostMapping("/aluguel-salao/solicitar")
    public String solicitarAluguel(
            @ModelAttribute("solicitacaoForm") SolicitacaoAluguelSalao solicitacao,
            RedirectAttributes redirectAttributes) {

        try {
            solicitacaoAluguelSalaoService.criarSolicitacaoPublica(solicitacao);
            redirectAttributes.addFlashAttribute("mensagem",
                    "Solicitação enviada com sucesso! A equipe da ASSANDEF entrará em contato pelo telefone ou e-mail informado caso a data e o horário estejam disponíveis.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao enviar solicitação: " + e.getMessage());
        }

        return "redirect:/aluguel-salao";
    }

    // =========================================================
    // ÁREA ADMINISTRATIVA / SECRETARIA
    // =========================================================

    @GetMapping("/aluguel-salao/gestao")
    public String exibirGestao(Model model) {
        popularModelGestao(model);
        return "aluguel-salao/gestao-aluguel-salao";
    }

    @PostMapping("/aluguel-salao/gestao/solicitacoes/{id}/status")
    public String atualizarStatusSolicitacao(
            @PathVariable Integer id,
            @RequestParam("status") StatusSolicitacaoAluguelSalao status,
            @RequestParam(value = "observacaoSecretaria", required = false) String observacaoSecretaria,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Funcionario funcionario = funcionarioService.findByLogin(authentication.getName());
            solicitacaoAluguelSalaoService.atualizarStatus(id, status, observacaoSecretaria, funcionario);
            redirectAttributes.addFlashAttribute("mensagem", "Status da solicitação atualizado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao atualizar solicitação: " + e.getMessage());
        }

        return "redirect:/aluguel-salao/gestao";
    }

    private void popularModelGestao(Model model) {
        List<SolicitacaoAluguelSalao> solicitacoes = solicitacaoAluguelSalaoService.findAll();

        long totalPendentes = solicitacoes.stream()
                .filter(sol -> sol.getStatus() == StatusSolicitacaoAluguelSalao.PENDENTE)
                .count();

        long totalAlugadas = solicitacoes.stream()
                .filter(sol -> sol.getStatus() == StatusSolicitacaoAluguelSalao.ALUGADO)
                .count();

        model.addAttribute("ativa", "aluguelSalao");
        model.addAttribute("solicitacoes", solicitacoes);
        model.addAttribute("totalSolicitacoes", solicitacoes.size());
        model.addAttribute("totalPendentes", totalPendentes);
        model.addAttribute("totalAlugadas", totalAlugadas);
        model.addAttribute("statusSolicitacao", StatusSolicitacaoAluguelSalao.values());
    }
}
