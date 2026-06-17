package br.org.assandef.assandefsystem.controller;

import br.org.assandef.assandefsystem.model.DisponibilidadeSalao;
import br.org.assandef.assandefsystem.model.Funcionario;
import br.org.assandef.assandefsystem.model.SolicitacaoAluguelSalao;
import br.org.assandef.assandefsystem.model.SolicitacaoAluguelSalao.StatusSolicitacaoAluguelSalao;
import br.org.assandef.assandefsystem.service.DisponibilidadeSalaoService;
import br.org.assandef.assandefsystem.service.FuncionarioService;
import br.org.assandef.assandefsystem.service.SolicitacaoAluguelSalaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AluguelSalaoController {

    private final DisponibilidadeSalaoService disponibilidadeSalaoService;
    private final SolicitacaoAluguelSalaoService solicitacaoAluguelSalaoService;
    private final FuncionarioService funcionarioService;

    // =========================================================
    // PÁGINA PÚBLICA
    // =========================================================

    @GetMapping("/aluguel-salao")
    public String exibirPaginaPublica(Model model) {
        model.addAttribute("ativa", "aluguelSalao");
        model.addAttribute("disponibilidades", disponibilidadeSalaoService.findDisponiveisParaPaginaPublica());
        model.addAttribute("solicitacaoForm", new SolicitacaoAluguelSalao());
        model.addAttribute("tiposDocumento", SolicitacaoAluguelSalao.TipoDocumento.values());
        return "aluguel-salao/aluguel-salao";
    }

    @PostMapping("/aluguel-salao/solicitar")
    public String solicitarAluguel(
            @RequestParam("idDisponibilidade") Integer idDisponibilidade,
            @ModelAttribute("solicitacaoForm") SolicitacaoAluguelSalao solicitacao,
            RedirectAttributes redirectAttributes) {

        try {
            solicitacaoAluguelSalaoService.criarSolicitacaoPublica(idDisponibilidade, solicitacao);
            redirectAttributes.addFlashAttribute("mensagem",
                    "Solicitação enviada com sucesso! A equipe da ASSANDEF entrará em contato para combinar o pagamento.");
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
        model.addAttribute("disponibilidadeForm", new DisponibilidadeSalao());
        return "aluguel-salao/gestao-aluguel-salao";
    }

    @PostMapping("/aluguel-salao/gestao/disponibilidades/salvar")
    public String salvarDisponibilidade(
            @ModelAttribute("disponibilidadeForm") DisponibilidadeSalao disponibilidade,
            RedirectAttributes redirectAttributes) {

        boolean isEdicao = disponibilidade.getIdDisponibilidade() != null;

        try {
            disponibilidadeSalaoService.save(disponibilidade);
            redirectAttributes.addFlashAttribute("mensagem",
                    isEdicao ? "Disponibilidade atualizada com sucesso!" : "Disponibilidade cadastrada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar disponibilidade: " + e.getMessage());
        }

        return "redirect:/aluguel-salao/gestao";
    }

    @GetMapping("/aluguel-salao/gestao/disponibilidades/json/{id}")
    @ResponseBody
    public DisponibilidadeSalao disponibilidadeJson(@PathVariable Integer id) {
        return disponibilidadeSalaoService.findById(id);
    }

    @PostMapping("/aluguel-salao/gestao/disponibilidades/excluir/{id}")
    public String excluirDisponibilidade(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            disponibilidadeSalaoService.deleteById(id);
            redirectAttributes.addFlashAttribute("mensagem", "Disponibilidade excluída com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir disponibilidade: " + e.getMessage());
        }

        return "redirect:/aluguel-salao/gestao";
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
        model.addAttribute("ativa", "aluguelSalao");
        model.addAttribute("disponibilidades", disponibilidadeSalaoService.findAll());
        model.addAttribute("solicitacoes", solicitacaoAluguelSalaoService.findAll());
        model.addAttribute("statusSolicitacao", StatusSolicitacaoAluguelSalao.values());
        model.addAttribute("statusDisponibilidade", DisponibilidadeSalao.StatusDisponibilidadeSalao.values());
    }
}
