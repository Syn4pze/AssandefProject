package br.org.assandef.assandefsystem.controller;

import br.org.assandef.assandefsystem.model.Categoria;
import br.org.assandef.assandefsystem.model.Funcionario;
import br.org.assandef.assandefsystem.model.Material;
import br.org.assandef.assandefsystem.model.SolicitacoesMaterial;
import br.org.assandef.assandefsystem.model.StatusSolicitacao;
import br.org.assandef.assandefsystem.repository.FuncionarioRepository;
import br.org.assandef.assandefsystem.service.CategoriaService;
import br.org.assandef.assandefsystem.service.FuncionarioService;
import br.org.assandef.assandefsystem.service.MaterialService;
import br.org.assandef.assandefsystem.service.SolicitacoesMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
@Controller
@RequestMapping("/almoxarifado")
@RequiredArgsConstructor
public class AlmoxarifadoViewController {
    private final CategoriaService categoriaService;
    private final MaterialService materialService;
    private final SolicitacoesMaterialService solicitacoesService;
    private final FuncionarioService funcionarioService;
    @Autowired
    private final FuncionarioRepository funcionarioRepository;
    // Página única
    @GetMapping
    public String pagina(Model model,
                         @ModelAttribute("msg") String msg,
                         @ModelAttribute("erro") String erro) {

        List<Material> materiais = materialService.findAll();           // ajuste se o método for listarTodos()
        List<Categoria> categorias = categoriaService.findAll();        // ajuste se o método for listarTodas()
        List<SolicitacoesMaterial> solicitacoes = solicitacoesService.findAll();
        List<Funcionario> funcionarios = funcionarioService.findAll();  // ajuste conforme seu service

        model.addAttribute("materiais", materiais);
        model.addAttribute("categorias", categorias);
        model.addAttribute("solicitacoes", solicitacoes);
        model.addAttribute("funcionarios", funcionarios);

        // se quiser usar th:object no modal de categoria
        model.addAttribute("categoria", new Categoria());

        if (msg != null && !msg.isBlank()) model.addAttribute("msg", msg);
        if (erro != null && !erro.isBlank()) model.addAttribute("erro", erro);

        return "almoxarifado/almoxarifado";
        // templates/almoxarifado/almoxarifado.html
    }

    // ======================
    // CATEGORIA
    // ======================
    @PostMapping("/categoria/salvar")
    public String salvarCategoria(@ModelAttribute Categoria categoria, RedirectAttributes ra) {
        try {
            categoriaService.save(categoria);
            ra.addFlashAttribute("msg", "Categoria salva com sucesso!");
        } catch (DataIntegrityViolationException e) {
            // Verifica se é erro de duplicação (constraint violation)
            if (e.getRootCause() != null && e.getRootCause().getMessage().contains("Duplicate entry")) {
                ra.addFlashAttribute("erro", "Erro: Já existe uma categoria com esse nome.");
            } else {
                ra.addFlashAttribute("erro", "Erro ao salvar categoria: " + e.getMessage());
            }
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao salvar categoria: " + e.getMessage());
        }
        return "redirect:/almoxarifado";
    }

    @PostMapping("/categoria/deletar/{id}")
    public String deletarCategoria(@PathVariable("id") Integer idCategoria, RedirectAttributes ra) {
        try {
            categoriaService.deleteById(idCategoria); // ajuste se for deletar(...)
            ra.addFlashAttribute("msg", "Categoria excluída com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao excluir categoria: " + e.getMessage());
        }
        return "redirect:/almoxarifado";
    }

    // ======================
    // MATERIAL
    // ======================
    @PostMapping("/material/salvar")
    public String salvarMaterial(@ModelAttribute Material material, RedirectAttributes ra) {
        try {
            // Se veio apenas categoria.idCategoria, resolvemos a entidade para consistência:
            if (material.getCategoria() != null && material.getCategoria().getIdCategoria() != null) {
                Integer idCat = material.getCategoria().getIdCategoria();
                Categoria cat = categoriaService.findById(idCat); // ajuste se o ID for Long e método buscarPorId(...)
                material.setCategoria(cat);
            }

            materialService.save(material); // create ou update
            ra.addFlashAttribute("msg", material.getIdMaterial() == null
                    ? "Material criado com sucesso!"
                    : "Material atualizado com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao salvar material: " + e.getMessage());
        }
        return "redirect:/almoxarifado";
    }

    @PostMapping("/material/deletar/{id}")
    public String deletarMaterial(@PathVariable("id") Integer idMaterial, RedirectAttributes ra) {
        try {
            materialService.deleteById(idMaterial);
            ra.addFlashAttribute("msg", "Material excluído com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao excluir material: " + e.getMessage());
        }
        return "redirect:/almoxarifado";
    }

    // JSON para preencher modal de edição
    @ResponseBody
    @GetMapping("/material/{id}")
    public ResponseEntity<Material> obterMaterial(@PathVariable("id") Integer idMaterial) {
        Material mat = materialService.findById(idMaterial);
        if (mat == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(mat);
    }

    // ======================
    // SOLICITAÇÕES DE MATERIAL
    // ======================
    @PostMapping("/solicitacao/salvar")
    public String salvarSolicitacao(@ModelAttribute SolicitacoesMaterial solicitacao,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes ra) {
        try {
            if (solicitacao.getStatus() == null) {
                solicitacao.setStatus(StatusSolicitacao.PENDENTE);
            }
            solicitacao.setDataSolicitacao(LocalDateTime.now());

            String loginUsuario = userDetails.getUsername();
            Funcionario funcionarioLogado = funcionarioRepository.findByLogin(loginUsuario)
                    .orElseThrow(() -> new RuntimeException("Funcionário logado não encontrado: " + loginUsuario));

            solicitacao.setFuncionarioSolicitante(funcionarioLogado);

            if (solicitacao.getMaterial() != null && solicitacao.getMaterial().getIdMaterial() != null) {
                Integer idMat = solicitacao.getMaterial().getIdMaterial();
                Material m = materialService.findById(idMat);
                solicitacao.setMaterial(m);
            }

            solicitacoesService.save(solicitacao);
            ra.addFlashAttribute("msg", "Solicitação registrada com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao registrar solicitação: " + e.getMessage());
        }
        return "redirect:/almoxarifado";
    }

    // Aprovar = mudar status para APROVADA e salvar
    @PostMapping("/solicitacao/aprovar/{id}")
    public String aprovarSolicitacao(@PathVariable("id") Integer idSolicitacao, RedirectAttributes ra) {
        try {
            SolicitacoesMaterial sol = solicitacoesService.findById(idSolicitacao);
            sol.setStatus(StatusSolicitacao.APROVADA);
            //Se você deseja reduzir estoque automaticamente:
            materialService.baixarEstoque(sol.getMaterial().getIdMaterial(), sol.getQuantidadeSolicitada());
            solicitacoesService.save(sol);
            ra.addFlashAttribute("msg", "Solicitação aprovada!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao aprovar solicitação: " + e.getMessage());
        }
        return "redirect:/almoxarifado";
    }

    // Recusar = mudar status para RECUSADA e salvar
    @PostMapping("/solicitacao/recusar/{id}")
    public String recusarSolicitacao(@PathVariable("id") Integer idSolicitacao, RedirectAttributes ra) {
        try {
            SolicitacoesMaterial sol = solicitacoesService.findById(idSolicitacao);
            sol.setStatus(StatusSolicitacao.REJEITADA);
            solicitacoesService.save(sol);
            ra.addFlashAttribute("msg", "Solicitação recusada.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao recusar solicitação: " + e.getMessage());
        }
        return "redirect:/almoxarifado";
    }

    @PostMapping("/solicitacao/deletar/{id}")
    public String deletarSolicitacao(@PathVariable("id") Integer idSolicitacao, RedirectAttributes ra) {
        try {
            solicitacoesService.deleteById(idSolicitacao);
            ra.addFlashAttribute("msg", "Solicitação excluída.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao excluir solicitação: " + e.getMessage());
        }
        return "redirect:/almoxarifado";
    }

    @GetMapping("/solicitacao/visualizar/{id}")
    @ResponseBody
    public SolicitacoesMaterial getSolicitacao(@PathVariable("id") Integer id) {
        return solicitacoesService.findById(id);
    }
}