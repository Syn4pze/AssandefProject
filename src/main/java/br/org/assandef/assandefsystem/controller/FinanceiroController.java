package br.org.assandef.assandefsystem.controller;

import br.org.assandef.assandefsystem.model.CategoriaFinanceira;
import br.org.assandef.assandefsystem.model.ContaBancaria;
import br.org.assandef.assandefsystem.model.MovimentacaoFinanceira;
import br.org.assandef.assandefsystem.service.CategoriaFinanceiraService;
import br.org.assandef.assandefsystem.service.ContaBancariaService;
import br.org.assandef.assandefsystem.service.MovimentacaoFinanceiraService;
import lombok.RequiredArgsConstructor;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

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

    @PostMapping("/contas/excluir/{id}")
    public String excluirContaPost(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            contaBancariaService.deleteById(id);
            redirectAttributes.addFlashAttribute("mensagem", "Conta excluída com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir conta: " + e.getMessage());
        }
        return "redirect:/financeiro";
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

    @PostMapping("/movimentacoes/excluir/{id}")
    public String excluirMovimentacaoPost(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            movimentacaoFinanceiraService.deleteById(id);
            redirectAttributes.addFlashAttribute("mensagem", "Movimentação excluída com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir movimentação: " + e.getMessage());
        }
        return "redirect:/financeiro";
    }
    @PostMapping("/movimentacoes/exportar")
    public void exportarMovimentacoes(
            @RequestParam(value = "idsContas", required = false) List<Integer> idsContas,
            @RequestParam("formato") String formato,
            HttpServletResponse response) throws IOException {

        List<MovimentacaoFinanceira> movimentacoes = movimentacaoFinanceiraService.findByContasParaRelatorio(idsContas);

        if ("csv".equalsIgnoreCase(formato)) {
            exportarMovimentacoesCsv(movimentacoes, idsContas, response);
            return;
        }

        if ("pdf".equalsIgnoreCase(formato)) {
            exportarMovimentacoesPdf(movimentacoes, idsContas, response);
            return;
        }

        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Formato de exportação inválido.");
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

    @PostMapping("/categorias/excluir/{id}")
    public String excluirCategoriaPost(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            categoriaFinanceiraService.deleteById(id);
            redirectAttributes.addFlashAttribute("mensagem", "Categoria excluída com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir categoria: " + e.getMessage());
        }
        return "redirect:/financeiro";
    }

    private void exportarMovimentacoesCsv(List<MovimentacaoFinanceira> movimentacoes, List<Integer> idsContas, HttpServletResponse response) throws IOException {
        StringBuilder csv = new StringBuilder();
        csv.append("Data;Tipo;Conta;Agência;Número da Conta;Categoria;Descrição;Valor\n");

        DateTimeFormatter dataFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (MovimentacaoFinanceira mov : movimentacoes) {
            String data = mov.getDataMovimentacao() != null ? mov.getDataMovimentacao().format(dataFormatter) : "";
            ContaBancaria conta = mov.getConta();
            CategoriaFinanceira categoria = mov.getCategoriaFinanceira();

            csv.append(escapeCsv(data)).append(';')
                    .append(escapeCsv(mov.getTipoMovimentacao() != null ? traduzirTipoMovimentacao(mov.getTipoMovimentacao()) : "")).append(';')
                    .append(escapeCsv(conta != null ? conta.getNomeBanco() : "")).append(';')
                    .append(escapeCsv(conta != null ? conta.getAgencia() : "")).append(';')
                    .append(escapeCsv(conta != null ? conta.getNumeroConta() : "")).append(';')
                    .append(escapeCsv(categoria != null ? categoria.getNome() : "")).append(';')
                    .append(escapeCsv(mov.getDescricao())).append(';')
                    .append(formatarMoedaCsv(mov.getValor()))
                    .append('\n');
        }

        byte[] bytes = ("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8);
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"movimentacoes_financeiras_" + LocalDate.now() + ".csv\"");
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
        response.getOutputStream().flush();
    }

    private void exportarMovimentacoesPdf(List<MovimentacaoFinanceira> movimentacoes, List<Integer> idsContas, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"movimentacoes_financeiras_" + LocalDate.now() + ".pdf\"");

        Document document = new Document(PageSize.A4.rotate(), 36, 36, 54, 36);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
            writer.setPageEvent(new PdfPageEventHelper() {
                @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, java.awt.Color.GRAY);
                    ColumnText.showTextAligned(
                            writer.getDirectContent(),
                            Element.ALIGN_CENTER,
                            new Phrase("Sistema de Gestão ASSANDEF - Relatório Financeiro", footerFont),
                            (document.right() + document.left()) / 2,
                            20,
                            0
                    );
                    ColumnText.showTextAligned(
                            writer.getDirectContent(),
                            Element.ALIGN_RIGHT,
                            new Phrase("Página " + writer.getPageNumber(), footerFont),
                            document.right(),
                            20,
                            0
                    );
                }
            });

            document.open();

            Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new java.awt.Color(13, 84, 148));
            Font subtituloFont = FontFactory.getFont(FontFactory.HELVETICA, 10, java.awt.Color.DARK_GRAY);
            Font resumoFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, java.awt.Color.BLACK);

            Paragraph titulo = new Paragraph("Relatório de Movimentações Financeiras", tituloFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(8f);
            document.add(titulo);

            Paragraph filtros = new Paragraph(descreverFiltroContas(idsContas), subtituloFont);
            filtros.setAlignment(Element.ALIGN_CENTER);
            filtros.setSpacingAfter(12f);
            document.add(filtros);

            BigDecimal totalEntradas = calcularTotalPorTipo(movimentacoes, MovimentacaoFinanceira.TipoMovimentacao.ENTRADA);
            BigDecimal totalSaidas = calcularTotalPorTipo(movimentacoes, MovimentacaoFinanceira.TipoMovimentacao.SAIDA);
            BigDecimal saldoMovimentado = totalEntradas.subtract(totalSaidas);

            PdfPTable resumo = new PdfPTable(3);
            resumo.setWidthPercentage(100);
            resumo.setSpacingAfter(14f);
            resumo.setWidths(new float[]{1f, 1f, 1f});
            resumo.addCell(createResumoCell("Total de entradas", formatarMoeda(totalEntradas), resumoFont));
            resumo.addCell(createResumoCell("Total de saídas", formatarMoeda(totalSaidas), resumoFont));
            resumo.addCell(createResumoCell("Saldo movimentado", formatarMoeda(saldoMovimentado), resumoFont));
            document.add(resumo);

            PdfPTable table = new PdfPTable(new float[]{1.2f, 1.1f, 2.0f, 1.6f, 2.8f, 1.4f});
            table.setWidthPercentage(100);
            table.setHeaderRows(1);
            addTableHeader(table, new String[]{"Data", "Tipo", "Conta", "Categoria", "Descrição", "Valor"});

            DateTimeFormatter dataFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (MovimentacaoFinanceira mov : movimentacoes) {
                table.addCell(createBodyCell(mov.getDataMovimentacao() != null ? mov.getDataMovimentacao().format(dataFormatter) : ""));
                table.addCell(createBodyCell(mov.getTipoMovimentacao() != null ? traduzirTipoMovimentacao(mov.getTipoMovimentacao()) : ""));
                table.addCell(createBodyCell(mov.getConta() != null ? montarNomeConta(mov.getConta()) : ""));
                table.addCell(createBodyCell(mov.getCategoriaFinanceira() != null ? mov.getCategoriaFinanceira().getNome() : ""));
                table.addCell(createBodyCell(mov.getDescricao() != null ? mov.getDescricao() : ""));
                table.addCell(createBodyCell(formatarMoeda(mov.getValor())));
            }

            if (movimentacoes.isEmpty()) {
                PdfPCell vazio = createBodyCell("Nenhuma movimentação encontrada para as contas selecionadas.");
                vazio.setColspan(6);
                vazio.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(vazio);
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            document.close();
            response.reset();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao gerar PDF: " + e.getMessage());
        }
    }

    private BigDecimal calcularTotalPorTipo(List<MovimentacaoFinanceira> movimentacoes, MovimentacaoFinanceira.TipoMovimentacao tipo) {
        return movimentacoes.stream()
                .filter(mov -> mov.getTipoMovimentacao() == tipo)
                .map(mov -> mov.getValor() != null ? mov.getValor() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String descreverFiltroContas(List<Integer> idsContas) {
        if (idsContas == null || idsContas.isEmpty()) {
            return "Contas selecionadas: todas as contas cadastradas | Emitido em: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }

        List<String> nomes = contaBancariaService.findAll().stream()
                .filter(conta -> idsContas.contains(conta.getIdConta()))
                .map(this::montarNomeConta)
                .collect(Collectors.toList());

        String contas = nomes.isEmpty() ? "nenhuma conta encontrada" : String.join(", ", nomes);
        return "Contas selecionadas: " + contas + " | Emitido em: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private String montarNomeConta(ContaBancaria conta) {
        if (conta == null) return "";
        String banco = conta.getNomeBanco() != null ? conta.getNomeBanco() : "Conta";
        String numero = conta.getNumeroConta() != null && !conta.getNumeroConta().isBlank() ? " - " + conta.getNumeroConta() : "";
        return banco + numero;
    }

    private String traduzirTipoMovimentacao(MovimentacaoFinanceira.TipoMovimentacao tipo) {
        if (tipo == null) return "";
        return tipo == MovimentacaoFinanceira.TipoMovimentacao.ENTRADA ? "Entrada" : "Saída";
    }

    private String formatarMoeda(BigDecimal valor) {
        BigDecimal valorSeguro = valor != null ? valor : BigDecimal.ZERO;
        return "R$ " + String.format(java.util.Locale.forLanguageTag("pt-BR"), "%,.2f", valorSeguro);
    }

    private String formatarMoedaCsv(BigDecimal valor) {
        BigDecimal valorSeguro = valor != null ? valor : BigDecimal.ZERO;
        return String.format(java.util.Locale.forLanguageTag("pt-BR"), "%.2f", valorSeguro);
    }

    private String escapeCsv(String valor) {
        if (valor == null) return "";
        String texto = valor.replace("\r", " ").replace("\n", " ");
        if (texto.contains(";") || texto.contains("\"") || texto.contains(",")) {
            return "\"" + texto.replace("\"", "\"\"") + "\"";
        }
        return texto;
    }

    private PdfPCell createResumoCell(String titulo, String valor, Font font) {
        Phrase phrase = new Phrase(titulo + "\n" + valor, font);
        PdfPCell cell = new PdfPCell(phrase);
        cell.setBackgroundColor(new java.awt.Color(240, 247, 255));
        cell.setBorderColor(new java.awt.Color(190, 210, 230));
        cell.setPadding(8f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private void addTableHeader(PdfPTable table, String[] headers) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, java.awt.Color.WHITE);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new java.awt.Color(13, 84, 148));
            cell.setPadding(6f);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private PdfPCell createBodyCell(String value) {
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 8, java.awt.Color.BLACK);
        PdfPCell cell = new PdfPCell(new Phrase(value != null ? value : "", bodyFont));
        cell.setPadding(5f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
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