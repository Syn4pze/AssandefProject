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

    @GetMapping
    @PreAuthorize("@authService.hasHierarquia(authentication, 1)")
    public String listar(Model model) {
        popularModelPublicacoes(model);
        return "gestao/publicacoes";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("@authService.hasHierarquia(authentication, 1)")
    public String editar(@PathVariable Integer id, Model model) {
        popularModelPublicacoes(model);
        model.addAttribute("publicacao", publicacaoService.buscarPorId(id));
        model.addAttribute("imagens", publicacaoService.listarImagensDaPublicacao(id));
        model.addAttribute("videos", publicacaoService.listarVideosDaPublicacao(id));
        return "gestao/publicacoes";
    }

    @PostMapping("/salvar")
    @PreAuthorize("@authService.hasHierarquia(authentication, 1)")
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
        }

        return "redirect:/gestao/publicacoes";
    }

    @PostMapping("/publicar/{id}")
    @PreAuthorize("@authService.hasHierarquia(authentication, 1)")
    public String publicar(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            publicacaoService.publicar(id);
            redirectAttributes.addFlashAttribute("sucesso", "Publicação publicada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao publicar: " + e.getMessage());
        }
        return "redirect:/gestao/publicacoes";
    }

    @PostMapping("/arquivar/{id}")
    @PreAuthorize("@authService.hasHierarquia(authentication, 1)")
    public String arquivar(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            publicacaoService.arquivar(id);
            redirectAttributes.addFlashAttribute("sucesso", "Publicação removida da página pública com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao ocultar publicação: " + e.getMessage());
        }
        return "redirect:/gestao/publicacoes";
    }

    @GetMapping({"/{id}/json", "/publicacoes/{id}/json"})
    @ResponseBody
    @PreAuthorize("@authService.hasHierarquia(authentication, 1)")
    public Map<String, Object> publicacaoJson(@PathVariable Integer id) {
        return publicacaoService.montarDadosJson(id);
    }

    @PostMapping("/imagem/excluir/{idImagem}")
    @PreAuthorize("@authService.hasHierarquia(authentication, 1)")
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

    @PostMapping("/video/excluir/{idVideo}")
    @PreAuthorize("@authService.hasHierarquia(authentication, 1)")
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

    @PostMapping("/excluir/{id}")
    @PreAuthorize("@authService.hasHierarquia(authentication, 1)")
    public String excluir(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            publicacaoService.listarImagensDaPublicacao(id)
                    .forEach(img -> {
                        try {
                            uploadService.excluirArquivo(img.getCaminhoArquivo());
                        } catch (Exception ignored) {
                        }
                    });

            publicacaoService.excluir(id);
            redirectAttributes.addFlashAttribute("sucesso", "Publicação excluída.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir: " + e.getMessage());
        }
        return "redirect:/gestao/publicacoes";
    }

    private void popularModelPublicacoes(Model model) {
        List<Publicacao> publicacoes = publicacaoService.listarTodas();
        model.addAttribute("publicacoes", publicacoes);
        model.addAttribute("imagensPorPublicacao", publicacaoService.mapearImagensPorPublicacao(publicacoes));
        model.addAttribute("publicacao", new Publicacao());
        model.addAttribute("tipos", Publicacao.TipoConteudo.values());
        model.addAttribute("proximosEventos", publicacoes.stream()
                .filter(p -> p.getTipoConteudo() == Publicacao.TipoConteudo.EVENTO
                        && p.getDataEvento() != null
                        && p.getDataEvento().isAfter(java.time.LocalDateTime.now()))
                .sorted(java.util.Comparator.comparing(Publicacao::getDataEvento))
                .limit(3)
                .toList());
        model.addAttribute("ativa", "publicacoes-gestao");
        model.addAttribute("modoGestao", true);
    }
}
