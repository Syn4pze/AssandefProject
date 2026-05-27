package br.org.assandef.assandefsystem.controller;

import br.org.assandef.assandefsystem.model.CategoriaFinanceira;
import br.org.assandef.assandefsystem.model.ContaBancaria;
import br.org.assandef.assandefsystem.model.MovimentacaoFinanceira;
import br.org.assandef.assandefsystem.service.CategoriaFinanceiraService;
import br.org.assandef.assandefsystem.service.ContaBancariaService;
import br.org.assandef.assandefsystem.service.MovimentacaoFinanceiraService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/financeiro")
@RequiredArgsConstructor
public class FinanceiroController {

    private final ContaBancariaService contaBancariaService;
    private final CategoriaFinanceiraService categoriaFinanceiraService;
    private final MovimentacaoFinanceiraService movimentacaoFinanceiraService;

    // =========================================================
    // PÁGINA PRINCIPAL
    // =========================================================

    @GetMapping
    public String exibirFinanceiro(Model model) {
        popularModel(model);
        model.addAttribute("contaForm", new ContaBancaria());
        model.addAttribute("movimentacaoForm", new MovimentacaoFinanceira());
        model.addAttribute("categoriaForm", new CategoriaFinanceira());
        return "financeiro/contabancaria";
    }

    // =========================================================
    // CONTAS BANCÁRIAS
    // =========================================================

    @PostMapping("/contas/salvar")
    public String salvarConta(
            @ModelAttribute("contaForm") ContaBancaria conta,
            RedirectAttributes redirectAttributes) {

        boolean isEdicao = conta.getIdConta() != null;
        try {
            contaBancariaService.save(conta);
            redirectAttributes.addFlashAttribute("mensagem",
                    isEdicao ? "Conta atualizada com sucesso!" : "Conta cadastrada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar conta: " + e.getMessage());
        }
        return "redirect:/financeiro";
    }

    // JSON para preencher modal de edição
    @GetMapping("/contas/json/{id}")
    @ResponseBody
    public ContaBancaria contaJson(@PathVariable Integer id) {
        return contaBancariaService.findById(id);
    }

    // GET para exclusão (chamado via window.location.href no JS)
    @GetMapping("/contas/excluir/{id}")
    public String excluirContaGet(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            contaBancariaService.deleteById(id);
            redirectAttributes.addFlashAttribute("mensagem", "Conta excluída com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir conta: " + e.getMessage());
        }
        return "redirect:/financeiro";
    }

    // POST mantido para compatibilidade (caso use form POST em algum lugar)
    @PostMapping("/contas/excluir/{id}")
    public String excluirContaPost(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        return excluirContaGet(id, redirectAttributes);
    }

    // =========================================================
    // MOVIMENTAÇÕES FINANCEIRAS
    // =========================================================

    @PostMapping("/movimentacoes/salvar")
    public String salvarMovimentacao(
            @RequestParam(value = "idMovimentacao", required = false) Integer idMovimentacao,
            @RequestParam(value = "idConta", required = false) Integer idConta,
            @RequestParam(value = "idCategoriaFinanceira", required = false) Integer idCategoriaFinanceira,
            @RequestParam(value = "tipoMovimentacao", required = false) String tipoMovimentacao,
            @RequestParam(value = "valor", required = false) BigDecimal valor,
            @RequestParam(value = "dataMovimentacao", required = false) String dataMovimentacao,
            @RequestParam(value = "descricao", required = false) String descricao,
            RedirectAttributes redirectAttributes) {

        try {
            // Busca existente (edição) ou cria nova
            MovimentacaoFinanceira mov = (idMovimentacao != null)
                    ? movimentacaoFinanceiraService.findById(idMovimentacao)
                    : new MovimentacaoFinanceira();

            if (idConta != null) mov.setConta(contaBancariaService.findById(idConta));
            if (idCategoriaFinanceira != null) mov.setCategoriaFinanceira(categoriaFinanceiraService.findById(idCategoriaFinanceira));
            if (tipoMovimentacao != null && !tipoMovimentacao.isBlank())
                mov.setTipoMovimentacao(MovimentacaoFinanceira.TipoMovimentacao.valueOf(tipoMovimentacao));
            if (valor != null) mov.setValor(valor);
            mov.setDataMovimentacao(java.time.LocalDate.now());
            mov.setDescricao(descricao);

            movimentacaoFinanceiraService.save(mov);
            boolean isEdicao = idMovimentacao != null;
            redirectAttributes.addFlashAttribute("mensagem",
                    isEdicao ? "Movimentação atualizada com sucesso!" : "Movimentação registrada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar movimentação: " + e.getMessage());
        }
        return "redirect:/financeiro";
    }

    // JSON para preencher modal de edição
    @GetMapping("/movimentacoes/json/{id}")
    @ResponseBody
    public MovimentacaoFinanceira movimentacaoJson(@PathVariable Integer id) {
        return movimentacaoFinanceiraService.findById(id);
    }

    // GET para exclusão (chamado via window.location.href no JS)
    @GetMapping("/movimentacoes/excluir/{id}")
    public String excluirMovimentacaoGet(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            movimentacaoFinanceiraService.deleteById(id);
            redirectAttributes.addFlashAttribute("mensagem", "Movimentação excluída com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir movimentação: " + e.getMessage());
        }
        return "redirect:/financeiro";
    }

    @PostMapping("/movimentacoes/excluir/{id}")
    public String excluirMovimentacaoPost(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        return excluirMovimentacaoGet(id, redirectAttributes);
    }

    // =========================================================
    // CATEGORIAS FINANCEIRAS
    // =========================================================

    @PostMapping("/categorias/salvar")
    public String salvarCategoria(
            @RequestParam(value = "idCategoriaFinanceira", required = false) Integer idCategoriaFinanceira,
            @RequestParam(value = "nome") String nome,
            @RequestParam(value = "tipo") String tipo,
            @RequestParam(value = "descricao", required = false) String descricao,
            RedirectAttributes redirectAttributes) {

        try {
            // Busca existente (edição) ou cria nova
            CategoriaFinanceira categoria = (idCategoriaFinanceira != null)
                    ? categoriaFinanceiraService.findById(idCategoriaFinanceira)
                    : new CategoriaFinanceira();

            categoria.setNome(nome);
            if (tipo != null && !tipo.isBlank())
                categoria.setTipo(CategoriaFinanceira.TipoCategoria.valueOf(tipo));
            categoria.setDescricao(descricao);

            categoriaFinanceiraService.save(categoria);
            boolean isEdicao = idCategoriaFinanceira != null;
            redirectAttributes.addFlashAttribute("mensagem",
                    isEdicao ? "Categoria atualizada com sucesso!" : "Categoria cadastrada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar categoria: " + e.getMessage());
        }
        return "redirect:/financeiro";
    }

    // GET para exclusão (chamado via window.location.href no JS)
    @GetMapping("/categorias/excluir/{id}")
    public String excluirCategoriaGet(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            categoriaFinanceiraService.deleteById(id);
            redirectAttributes.addFlashAttribute("mensagem", "Categoria excluída com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir categoria: " + e.getMessage());
        }
        return "redirect:/financeiro";
    }

    @PostMapping("/categorias/excluir/{id}")
    public String excluirCategoriaPost(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        return excluirCategoriaGet(id, redirectAttributes);
    }

    // =========================================================
    // MÉTODO AUXILIAR
    // =========================================================

    private void popularModel(Model model) {
        List<ContaBancaria> contas = contaBancariaService.findAll();
        List<MovimentacaoFinanceira> movimentacoes = movimentacaoFinanceiraService.findAll();
        List<CategoriaFinanceira> categorias = categoriaFinanceiraService.findAll();

        BigDecimal totalEntradas = movimentacoes.stream()
                .filter(m -> m.getTipoMovimentacao() == MovimentacaoFinanceira.TipoMovimentacao.ENTRADA)
                .map(m -> m.getValor() != null ? m.getValor() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSaidas = movimentacoes.stream()
                .filter(m -> m.getTipoMovimentacao() == MovimentacaoFinanceira.TipoMovimentacao.SAIDA)
                .map(m -> m.getValor() != null ? m.getValor() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldoGeral = contas.stream()
                .map(c -> c.getSaldo() != null ? c.getSaldo() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("contas", contas);
        model.addAttribute("movimentacoes", movimentacoes);
        model.addAttribute("categorias", categorias);
        model.addAttribute("totalEntradas", totalEntradas);
        model.addAttribute("totalSaidas", totalSaidas);
        model.addAttribute("saldoGeral", saldoGeral);
    }
}