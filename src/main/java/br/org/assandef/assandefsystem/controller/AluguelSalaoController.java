package br.org.assandef.assandefsystem.controller;

import br.org.assandef.assandefsystem.model.Funcionario;
import br.org.assandef.assandefsystem.model.SolicitacaoAluguelSalao;
import br.org.assandef.assandefsystem.model.SolicitacaoAluguelSalao.StatusSolicitacaoAluguelSalao;
import br.org.assandef.assandefsystem.service.FotoSalaoService;
import br.org.assandef.assandefsystem.service.FuncionarioService;
import br.org.assandef.assandefsystem.service.PlanoAluguelSalaoService;
import br.org.assandef.assandefsystem.service.SolicitacaoAluguelSalaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AluguelSalaoController {

    private final SolicitacaoAluguelSalaoService solicitacaoAluguelSalaoService;
    private final PlanoAluguelSalaoService planoAluguelSalaoService;
    private final FotoSalaoService fotoSalaoService;
    private final FuncionarioService funcionarioService;

    // =========================================================
    // PÁGINA PÚBLICA
    // =========================================================

    @GetMapping("/aluguel-salao")
    public String exibirPaginaPublica(Model model) {
        model.addAttribute("ativa", "aluguelSalao");
        model.addAttribute("solicitacaoForm", new SolicitacaoAluguelSalao());
        model.addAttribute("tiposDocumento", SolicitacaoAluguelSalao.TipoDocumento.values());
        model.addAttribute("planosAluguel", planoAluguelSalaoService.findAtivos());
        model.addAttribute("fotosSalao", fotoSalaoService.findAtivas());
        model.addAttribute("datasOcupadas", solicitacaoAluguelSalaoService.findDatasOcupadasIso());
        return "aluguel-salao/aluguel-salao";
    }

    @GetMapping("/aluguel-salao/datas-ocupadas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarDatasOcupadas() {
        return ResponseEntity.ok(Map.of("datasOcupadas", solicitacaoAluguelSalaoService.findDatasOcupadasIso()));
    }

    @PostMapping("/aluguel-salao/solicitar")
    public String solicitarAluguel(
            @ModelAttribute("solicitacaoForm") SolicitacaoAluguelSalao solicitacao,
            @RequestParam(value = "idPlanoAluguel", required = false) Integer idPlanoAluguel,
            RedirectAttributes redirectAttributes) {

        try {
            solicitacaoAluguelSalaoService.criarSolicitacaoPublica(solicitacao, idPlanoAluguel);
            redirectAttributes.addFlashAttribute("mensagem",
                    "Solicitação enviada com sucesso! A equipe da ASSANDEF entrará em contato pelo telefone ou e-mail informado para confirmar a disponibilidade e dar continuidade ao atendimento.");
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

    @PostMapping("/aluguel-salao/gestao/planos/salvar")
    public String salvarPlano(
            @RequestParam(value = "idPlano", required = false) Integer idPlano,
            @RequestParam("nomePlano") String nomePlano,
            @RequestParam("valor") BigDecimal valor,
            @RequestParam("itensInclusos") String itensInclusos,
            @RequestParam(value = "descricao", required = false) String descricao,
            @RequestParam(value = "ativo", defaultValue = "false") boolean ativo,
            RedirectAttributes redirectAttributes) {

        try {
            planoAluguelSalaoService.salvar(idPlano, nomePlano, valor, itensInclusos, descricao, ativo);
            redirectAttributes.addFlashAttribute("mensagem", "Plano de aluguel salvo com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar plano: " + e.getMessage());
        }

        return "redirect:/aluguel-salao/gestao#planos";
    }

    @PostMapping("/aluguel-salao/gestao/planos/{id}/status")
    public String alterarStatusPlano(
            @PathVariable Integer id,
            @RequestParam("ativo") boolean ativo,
            RedirectAttributes redirectAttributes) {

        try {
            planoAluguelSalaoService.alterarStatus(id, ativo);
            redirectAttributes.addFlashAttribute("mensagem", ativo ? "Plano ativado com sucesso!" : "Plano desativado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao alterar plano: " + e.getMessage());
        }

        return "redirect:/aluguel-salao/gestao#planos";
    }

    @PostMapping("/aluguel-salao/gestao/planos/{id}/excluir")
    public String excluirPlano(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            planoAluguelSalaoService.deleteById(id);
            redirectAttributes.addFlashAttribute("mensagem", "Plano excluído com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Não foi possível excluir o plano. Se ele já estiver vinculado a uma solicitação, desative o plano em vez de excluí-lo.");
        }

        return "redirect:/aluguel-salao/gestao#planos";
    }

    @PostMapping("/aluguel-salao/gestao/fotos/salvar")
    public String salvarFoto(
            @RequestParam(value = "idFoto", required = false) Integer idFoto,
            @RequestParam(value = "titulo", required = false) String titulo,
            @RequestParam(value = "descricao", required = false) String descricao,
            @RequestParam(value = "ordemExibicao", required = false) Integer ordemExibicao,
            @RequestParam(value = "fotoPrincipal", defaultValue = "false") boolean fotoPrincipal,
            @RequestParam(value = "ativo", defaultValue = "false") boolean ativo,
            @RequestParam(value = "arquivo", required = false) MultipartFile arquivo,
            RedirectAttributes redirectAttributes) {

        try {
            fotoSalaoService.salvar(idFoto, titulo, descricao, ordemExibicao, fotoPrincipal, ativo, arquivo);
            redirectAttributes.addFlashAttribute("mensagem", "Foto do salão salva com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar foto: " + e.getMessage());
        }

        return "redirect:/aluguel-salao/gestao#fotos";
    }

    @PostMapping("/aluguel-salao/gestao/fotos/{id}/principal")
    public String definirFotoPrincipal(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            fotoSalaoService.definirPrincipal(id);
            redirectAttributes.addFlashAttribute("mensagem", "Foto principal atualizada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao definir foto principal: " + e.getMessage());
        }

        return "redirect:/aluguel-salao/gestao#fotos";
    }

    @PostMapping("/aluguel-salao/gestao/fotos/{id}/status")
    public String alterarStatusFoto(
            @PathVariable Integer id,
            @RequestParam("ativo") boolean ativo,
            RedirectAttributes redirectAttributes) {

        try {
            fotoSalaoService.alterarStatus(id, ativo);
            redirectAttributes.addFlashAttribute("mensagem", ativo ? "Foto ativada com sucesso!" : "Foto desativada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao alterar foto: " + e.getMessage());
        }

        return "redirect:/aluguel-salao/gestao#fotos";
    }

    @PostMapping("/aluguel-salao/gestao/fotos/{id}/excluir")
    public String excluirFoto(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            fotoSalaoService.deleteById(id);
            redirectAttributes.addFlashAttribute("mensagem", "Foto excluída com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir foto: " + e.getMessage());
        }

        return "redirect:/aluguel-salao/gestao#fotos";
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
        model.addAttribute("planosAluguel", planoAluguelSalaoService.findAll());
        model.addAttribute("fotosSalao", fotoSalaoService.findAll());
        model.addAttribute("datasOcupadas", solicitacaoAluguelSalaoService.findDatasOcupadasIso());
    }
}
