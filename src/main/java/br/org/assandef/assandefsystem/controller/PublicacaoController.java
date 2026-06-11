package br.org.assandef.assandefsystem.controller;

import br.org.assandef.assandefsystem.model.Funcionario;
import br.org.assandef.assandefsystem.model.Publicacao;
import br.org.assandef.assandefsystem.model.PublicacaoImagem;
import br.org.assandef.assandefsystem.model.PublicacaoVideo;
import br.org.assandef.assandefsystem.repository.FuncionarioRepository;
import br.org.assandef.assandefsystem.service.PublicacaoService;
import br.org.assandef.assandefsystem.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/gestao/publicacoes")
@RequiredArgsConstructor
public class PublicacaoController {

    private final PublicacaoService publicacaoService;
    private final UploadService uploadService;
    private final FuncionarioRepository funcionarioRepository;

    // ── VIEWS ─────────────────────────────────────────────────

    // Listagem interna (somente Admin)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String listar(Model model) {
        model.addAttribute("publicacoes", publicacaoService.listarTodas());
        model.addAttribute("ativa", "publicacoes-gestao");
        return "gestao/publicacoes"; // ← era "gestao/publicacoes-lista"
    }

    // Formulário de edição
    @GetMapping("/editar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editar(@PathVariable Integer id, Model model) {
        model.addAttribute("publicacao", publicacaoService.buscarPorId(id));
        model.addAttribute("tipos", Publicacao.TipoConteudo.values());
        model.addAttribute("imagens", publicacaoService.listarImagensDaPublicacao(id));
        model.addAttribute("videos", publicacaoService.listarVideosDaPublicacao(id));
        model.addAttribute("ativa", "publicacoes-gestao");
        return "gestao/publicacoes";
    }

    // ── AÇÕES ─────────────────────────────────────────────────

    // Salvar (criação e edição)
    @PostMapping("/salvar")
    @PreAuthorize("hasRole('ADMIN')")
    public String salvar(Publicacao publicacao,
                         @RequestParam(value = "arquivos", required = false) List<MultipartFile> arquivos,
                         @RequestParam(value = "linksYoutube", required = false) List<String> linksYoutube,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        try {
            Funcionario autor = funcionarioRepository.findByLogin(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Autor não encontrado"));

            Publicacao destino;
            if (publicacao.getIdPublicacao() != null) {
                Publicacao existente = publicacaoService.buscarPorId(publicacao.getIdPublicacao());
                existente.setTitulo(publicacao.getTitulo());
                existente.setDescricao(publicacao.getDescricao());
                existente.setConteudo(publicacao.getConteudo());
                existente.setTipoConteudo(publicacao.getTipoConteudo());
                existente.setDataEvento(publicacao.getDataEvento());
                existente.setLocalEvento(publicacao.getLocalEvento());
                existente.setFuncionarioAutor(existente.getFuncionarioAutor() != null ? existente.getFuncionarioAutor() : autor);
                existente.setDataAtualizacao(LocalDateTime.now());
                destino = publicacaoService.salvar(existente);
            } else {
                publicacao.setFuncionarioAutor(autor);
                publicacao.setDataAtualizacao(LocalDateTime.now());
                destino = publicacaoService.salvar(publicacao);
            }

            if (arquivos != null) {
                short ordem = (short) (publicacaoService.listarImagensDaPublicacao(destino.getIdPublicacao()).size() + 1);
                for (MultipartFile file : arquivos) {
                    if (!file.isEmpty()) {
                        String caminho = uploadService.salvarImagem(file, destino.getIdPublicacao());

                        PublicacaoImagem img = new PublicacaoImagem();
                        img.setPublicacao(destino);
                        img.setCaminhoArquivo(caminho);
                        img.setNomeOriginal(file.getOriginalFilename());
                        img.setTipoMime(file.getContentType());
                        img.setTamanhoBytes(file.getSize());
                        img.setOrdemExibicao(ordem);
                        img.setImagemPrincipal(ordem == 1);

                        publicacaoService.salvarImagem(img);
                        ordem++;
                    }
                }
            }

            if (linksYoutube != null) {
                publicacaoService.excluirVideosDaPublicacao(destino.getIdPublicacao());
                short ordemVid = 1;
                for (String url : linksYoutube) {
                    if (url != null && !url.isBlank()) {
                        PublicacaoVideo vid = new PublicacaoVideo();
                        vid.setPublicacao(destino);
                        vid.setUrlYoutube(url.trim());
                        vid.setOrdemExibicao(ordemVid++);
                        publicacaoService.salvarVideo(vid);
                    }
                }
            }

            String mensagem = publicacao.getIdPublicacao() != null
                    ? "Publicação atualizada com sucesso!"
                    : "Publicação salva com sucesso!";
            redirectAttributes.addFlashAttribute("sucesso", mensagem);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar: " + e.getMessage());
            return "redirect:/gestao/publicacoes";
        }

        return "redirect:/gestao/publicacoes";
    }

    // Publicar (muda status para PUBLICADA)
    @PostMapping("/publicar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String publicar(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            publicacaoService.publicar(id);
            redirectAttributes.addFlashAttribute("sucesso", "Publicação publicada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao publicar: " + e.getMessage());
        }
        return "redirect:/gestao/publicacoes";
    }

    @GetMapping("/publicacoes/{id}/json")
    @ResponseBody
    public Map<String, Object> publicacaoJson(@PathVariable Integer id) {
        Publicacao pub = publicacaoService.buscarPorId(id);
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        map.put("id",       pub.getIdPublicacao());
        map.put("titulo",   pub.getTitulo());
        map.put("descricao",pub.getDescricao());
        map.put("conteudo", pub.getConteudo());
        map.put("tipo",     pub.getTipoConteudo().name());
        map.put("data",     pub.getDataCriacao() != null
                ? pub.getDataCriacao().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH'h'mm"))
                : "");
        map.put("imagens",  pub.getImagens() == null ? List.of() :
                pub.getImagens().stream()
                        .map(PublicacaoImagem::getCaminhoArquivo)
                        .toList());
        map.put("videos", pub.getVideos() == null ? List.of() :
                pub.getVideos().stream()
                        .map(PublicacaoVideo::getUrlYoutube)
                        .toList());
        map.put("localEvento", pub.getLocalEvento());
        map.put("dataEvento",  pub.getDataEvento() != null
                ? pub.getDataEvento().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH'h'mm"))
                : null);
        map.put("dataEventoIso", pub.getDataEvento() != null
                ? pub.getDataEvento().toString()
                : null);
        return map;
    }
    // Excluir imagem individual
    @PostMapping("/imagem/excluir/{idImagem}")
    @PreAuthorize("hasRole('ADMIN')")
    public String excluirImagem(@PathVariable Integer idImagem,
                                @RequestParam Integer idPublicacao,
                                RedirectAttributes redirectAttributes) {
        try {
            PublicacaoImagem img = publicacaoService.listarImagensDaPublicacao(idPublicacao)
                    .stream()
                    .filter(i -> i.getIdImagem().equals(idImagem))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Imagem não encontrada"));

            uploadService.excluirArquivo(img.getCaminhoArquivo());
            publicacaoService.excluirImagem(idImagem);
            redirectAttributes.addFlashAttribute("sucesso", "Imagem removida.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao remover imagem: " + e.getMessage());
        }
        return "redirect:/gestao/publicacoes/editar/" + idPublicacao;
    }

    // Excluir vídeo individual
    @PostMapping("/video/excluir/{idVideo}")
    @PreAuthorize("hasRole('ADMIN')")
    public String excluirVideo(@PathVariable Integer idVideo,
                               @RequestParam Integer idPublicacao,
                               RedirectAttributes redirectAttributes) {
        try {
            publicacaoService.excluirVideo(idVideo);
            redirectAttributes.addFlashAttribute("sucesso", "Vídeo removido.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao remover vídeo: " + e.getMessage());
        }
        return "redirect:/gestao/publicacoes/editar/" + idPublicacao;
    }

    // Excluir publicação completa
    @PostMapping("/excluir/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String excluir(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            // Remove arquivos físicos das imagens antes de excluir
            publicacaoService.listarImagensDaPublicacao(id)
                    .forEach(img -> {
                        try {
                            uploadService.excluirArquivo(img.getCaminhoArquivo());
                        } catch (Exception ignored) {}
                    });

            publicacaoService.excluir(id);
            redirectAttributes.addFlashAttribute("sucesso", "Publicação excluída.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir: " + e.getMessage());
        }
        return "redirect:/gestao/publicacoes";
    }
}