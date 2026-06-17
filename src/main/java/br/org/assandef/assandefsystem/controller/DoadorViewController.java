package br.org.assandef.assandefsystem.controller;

import br.org.assandef.assandefsystem.model.Boleto;
import br.org.assandef.assandefsystem.model.Doador;
import br.org.assandef.assandefsystem.model.StatusBoleto;
import br.org.assandef.assandefsystem.security.AuthService;
import br.org.assandef.assandefsystem.service.BoletoService;
import br.org.assandef.assandefsystem.service.DoadorService;
import br.org.assandef.assandefsystem.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/doadores")
@RequiredArgsConstructor
public class DoadorViewController {

    private final ApplicationContext applicationContext;
    private final DoadorService doadorService;
    private final BoletoService boletoService;
    private final FileStorageService fileStorageService;
    private final AuthService authService;

    // ======================
    // PÁGINAS PRINCIPAIS
    // ======================

    // Página principal de administração (com modais de edição)
    @GetMapping
    public String paginaAdministrativa(
            @RequestParam(name = "pageDoadores", defaultValue = "0") int pageDoadores,
            @RequestParam(name = "sizeDoadores", defaultValue = "10") int sizeDoadores,
            @RequestParam(name = "pageBoletos", defaultValue = "0") int pageBoletos,
            @RequestParam(name = "sizeBoletos", defaultValue = "10") int sizeBoletos,
            Model model,
            @ModelAttribute("msg") String msg,
            @ModelAttribute("erro") String erro) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        Page<Doador> doadoresPage = doadorService.findAll(PageRequest.of(pageDoadores, sizeDoadores, Sort.by(Sort.Direction.DESC, "idDoador")));
        Page<Boleto> boletosPage = boletoService.findAll(PageRequest.of(pageBoletos, sizeBoletos, Sort.by(Sort.Direction.DESC, "idBoleto")));

        model.addAttribute("doadores", doadoresPage.getContent());
        model.addAttribute("doadoresSelect", doadorService.findAll());
        model.addAttribute("boletos", boletosPage.getContent());
        model.addAttribute("doadoresPage", doadoresPage);
        model.addAttribute("boletosPage", boletosPage);
        model.addAttribute("doador", new Doador());
        model.addAttribute("boleto", new Boleto());

        if (msg != null && !msg.isBlank()) model.addAttribute("msg", msg);
        if (erro != null && !erro.isBlank()) model.addAttribute("erro", erro);

        return "doadores/donation"; // página com modais de edição
    }

    // Página pública de cadastro
    @GetMapping("/newdonation")
    public String paginaCadastroPublico(Model model,
                                        @ModelAttribute("msg") String msg,
                                        @ModelAttribute("erro") String erro) {
        model.addAttribute("doador", new Doador());

        if (msg != null && !msg.isBlank()) model.addAttribute("msg", msg);
        if (erro != null && !erro.isBlank()) model.addAttribute("erro", erro);

        return "doadores/newdonation"; // página de cadastro público
    }

    // ======================
    // CADASTRO PÚBLICO
    // ======================

    @PostMapping("/salvar")
    public String salvarDoadorPublico(@ModelAttribute Doador doador, RedirectAttributes ra) {
        try {
            // Somente criação (não aceita id)
            if (doador.getIdDoador() != null) {
                ra.addFlashAttribute("erro", "Cadastro público não permite edição.");
                return "redirect:/doadores/newdonation";
            }

            doador.setDataCadastro(LocalDate.now());

            boolean exists = doadorService.existsByCpfCnpjOrEmailOrTelefoneExcludingId(
                    doador.getCpfCnpj(), doador.getEmail(), doador.getTelefone(), null);

            if (exists) {
                ra.addFlashAttribute("erro", "Já existe um cadastro com este CPF/CNPJ, email ou telefone.");
                return "redirect:/doadores/newdonation";
            }

            doadorService.save(doador);
            ra.addFlashAttribute("msg", "Cadastro realizado com sucesso! Entraremos em contato para mais informações");
            return "redirect:/doadores/newdonation";

        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao salvar doador: " + e.getMessage());
            return "redirect:/doadores/newdonation";
        }
    }

    // ======================
    // EDIÇÃO ADMINISTRATIVA (via modal — só JSON)
    // ======================

    // Endpoint JSON para carregar dados do doador (usado pelo modal)
    @ResponseBody
    @GetMapping("/{id}")
    public ResponseEntity<Doador> obterDoador(@PathVariable("id") Integer idDoador) {
        try {
            Doador doador = doadorService.findById(idDoador);
            return ResponseEntity.ok(doador);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Endpoint para salvar edição (via POST /editar/{id})
    @PostMapping("/editar/{id}")
    public String salvarEdicao(@PathVariable Integer id,
                               @ModelAttribute Doador doador,
                               RedirectAttributes ra) {
        try {
            // ... validações de auth ...

            // garantir id coerente
            doador.setIdDoador(id);

            boolean exists = doadorService.existsByCpfCnpjOrEmailOrTelefoneExcludingId(
                    doador.getCpfCnpj(), doador.getEmail(), doador.getTelefone(), id);

            if (exists) {
                ra.addFlashAttribute("erro", "Já existe um cadastro com este CPF/CNPJ, email ou telefone.");
                return "redirect:/doadores";
            }

            // chama update (carrega existente e copia campos)
            doadorService.update(id, doador);

            ra.addFlashAttribute("msg", "Doador atualizado com sucesso!");
            return "redirect:/doadores";
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao atualizar doador: " + e.getMessage());
            return "redirect:/doadores";
        }
    }

    // ======================
    // EXCLUSÃO
    // ======================

    @PostMapping("/deletar/{id}")
    public String deletarDoador(@PathVariable("id") Integer idDoador, RedirectAttributes ra) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
                ra.addFlashAttribute("erro", "Você precisa estar logado para excluir doadores.");
                return "redirect:/login";
            }
            if (!authService.hasAnyHierarquia(auth, 1, 3)) {
                ra.addFlashAttribute("erro", "Você não tem permissão para excluir doadores.");
                return "redirect:/doadores";
            }

            doadorService.deleteById(idDoador);
            ra.addFlashAttribute("msg", "Doador excluído com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao excluir doador: " + e.getMessage());
        }
        return "redirect:/doadores";
    }

    // ======================
    // APIs REST (JSON) - BOLETOS
    // ======================

    @ResponseBody
    @GetMapping("/{id}/boletos")
    public ResponseEntity<List<Boleto>> listarBoletosPorDoador(@PathVariable("id") Integer idDoador) {
        try {
            List<Boleto> boletos = boletoService.findByDoador(idDoador);
            return ResponseEntity.ok(boletos);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ======================
    // BOLETOS (UPLOAD, DOWNLOAD, STATUS)
    // ======================

    @PostMapping("/boleto/salvar")
    public String salvarBoleto(
            @RequestParam(value = "arquivo", required = false) MultipartFile arquivo,
            @ModelAttribute Boleto boleto,
            RedirectAttributes ra) {
        try {
            boolean isEdicao = (boleto.getIdBoleto() != null);
            String caminhoArquivoAntigo = null;

            if (isEdicao) {
                Boleto boletoExistente = boletoService.findById(boleto.getIdBoleto());
                caminhoArquivoAntigo = boletoExistente.getPdfBoleto();
            }

            if (boleto.getDoador() != null && boleto.getDoador().getIdDoador() != null) {
                Integer idDoador = boleto.getDoador().getIdDoador();
                try {
                    Doador doador = doadorService.findById(idDoador);
                    boleto.setDoador(doador);
                } catch (RuntimeException e) {
                    ra.addFlashAttribute("erro", "Doador informado não encontrado.");
                    return "redirect:/doadores";
                }
            }

            if (boleto.getStatus() == null) {
                boleto.setStatus(StatusBoleto.PENDENTE);
            }

            if (isEdicao && (arquivo == null || arquivo.isEmpty())) {
                boleto.setPdfBoleto(caminhoArquivoAntigo);
            }

            Boleto boletoSalvo = boletoService.save(boleto);

            if (arquivo != null && !arquivo.isEmpty()) {
                if (isEdicao && caminhoArquivoAntigo != null && !caminhoArquivoAntigo.isBlank()) {
                    try {
                        fileStorageService.deletarBoleto(caminhoArquivoAntigo);
                    } catch (Exception ignored) {}
                }

                String caminhoArquivo = fileStorageService.salvarBoleto(
                        arquivo,
                        boletoSalvo.getDoador().getIdDoador(),
                        boletoSalvo.getIdBoleto()
                );
                boletoSalvo.setPdfBoleto(caminhoArquivo);
                boletoService.save(boletoSalvo);
            }

            ra.addFlashAttribute("msg", isEdicao
                    ? "Boleto atualizado com sucesso!"
                    : "Boleto cadastrado com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao salvar boleto: " + e.getMessage());
        }
        return "redirect:/doadores";
    }

    @PostMapping("/boleto/deletar/{id}")
    public String deletarBoleto(@PathVariable("id") Integer idBoleto, RedirectAttributes ra) {
        try {
            Boleto boleto = boletoService.findById(idBoleto);

            if (boleto.getPdfBoleto() != null && !boleto.getPdfBoleto().isBlank()) {
                try {
                    fileStorageService.deletarBoleto(boleto.getPdfBoleto());
                } catch (Exception ignored) {}
            }

            boletoService.deleteById(idBoleto);
            ra.addFlashAttribute("msg", "Boleto excluído com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao excluir boleto: " + e.getMessage());
        }
        return "redirect:/doadores";
    }

    @ResponseBody
    @GetMapping("/boleto/{id}")
    public ResponseEntity<Boleto> obterBoleto(@PathVariable("id") Integer idBoleto) {
        Boleto boleto = boletoService.findById(idBoleto);
        if (boleto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(boleto);
    }

    @GetMapping("/boleto/download/{id}")
    public ResponseEntity<Resource> downloadBoleto(@PathVariable("id") Integer idBoleto) {
        try {
            Boleto boleto = boletoService.findById(idBoleto);

            if (boleto.getPdfBoleto() == null || boleto.getPdfBoleto().isBlank()) {
                return ResponseEntity.notFound().build();
            }

            Path arquivo = fileStorageService.obterArquivo(boleto.getPdfBoleto());
            Resource resource = new UrlResource(arquivo.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + arquivo.getFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/boleto/marcar-pago/{id}")
    public String marcarBoletoComoPago(@PathVariable("id") Integer idBoleto, RedirectAttributes ra) {
        try {
            Boleto boleto = boletoService.findById(idBoleto);
            boleto.setStatus(StatusBoleto.PAGO);
            boletoService.save(boleto);
            ra.addFlashAttribute("msg", "Boleto marcado como pago!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao atualizar boleto: " + e.getMessage());
        }
        return "redirect:/doadores";
    }

    @PostMapping("/boleto/marcar-vencido/{id}")
    public String marcarBoletoComoVencido(@PathVariable("id") Integer idBoleto, RedirectAttributes ra) {
        try {
            Boleto boleto = boletoService.findById(idBoleto);
            boleto.setStatus(StatusBoleto.VENCIDO);
            boletoService.save(boleto);
            ra.addFlashAttribute("msg", "Boleto marcado como vencido!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao atualizar boleto: " + e.getMessage());
        }
        return "redirect:/doadores";
    }
}