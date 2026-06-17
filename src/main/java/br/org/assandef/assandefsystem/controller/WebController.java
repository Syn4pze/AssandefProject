package br.org.assandef.assandefsystem.controller;

import br.org.assandef.assandefsystem.model.Publicacao;
import br.org.assandef.assandefsystem.service.PublicacaoService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Controller
public class WebController {

    private final PublicacaoService publicacaoService;

    @GetMapping("/")
    public String index(Model model, HttpServletResponse response) {
        desabilitarCache(response);
        List<Publicacao> ultimasPublicacoes = publicacaoService.listarPublicadas().stream()
                .limit(3)
                .toList();
        model.addAttribute("ativa", "inicio");
        model.addAttribute("ultimasPublicacoes", ultimasPublicacoes);
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "acesso/login";
    }

    @GetMapping("/profissional")
    public String pacientes() {
        return "profissional/profissional";
    }

    @GetMapping("/sobre")
    public String sobre() {
        return "home/sobre";
    }

    @GetMapping("/publicacoes")
    public String publicacoes(Model model, HttpServletResponse response) {
        desabilitarCache(response);
        List<Publicacao> publicadas = publicacaoService.listarPublicadas();

        model.addAttribute("ativa", "publicacoes");
        model.addAttribute("modoGestao", false);
        model.addAttribute("tipos", Publicacao.TipoConteudo.values());
        model.addAttribute("publicacoes", publicadas);
        Map<Integer, List<String>> imagensPorPublicacao = publicacaoService.mapearImagensPorPublicacao(publicadas);
        model.addAttribute("imagensPorPublicacao", imagensPorPublicacao);
        try {
            model.addAttribute("imagensPorPublicacaoJson",
                    new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(imagensPorPublicacao));
        } catch (Exception e) {
            model.addAttribute("imagensPorPublicacaoJson", "{}");
        }
        model.addAttribute("proximosEventos", publicadas.stream()
                .filter(p -> p.getTipoConteudo() == Publicacao.TipoConteudo.EVENTO
                        && p.getDataEvento() != null
                        && p.getDataEvento().isAfter(java.time.LocalDateTime.now()))
                .sorted(java.util.Comparator.comparing(Publicacao::getDataEvento))
                .limit(3)
                .toList());
        return "gestao/publicacoes";
    }

    @GetMapping("/publicacoes/{id}/json")
    @ResponseBody
    public Map<String, Object> publicacaoJson(@PathVariable Integer id, HttpServletResponse response) {
        desabilitarCache(response);
        return publicacaoService.montarDadosPublicoJson(id);
    }

    private void desabilitarCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }
}
