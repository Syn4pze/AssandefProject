package br.org.assandef.assandefsystem.controller;

import br.org.assandef.assandefsystem.model.Publicacao;
import br.org.assandef.assandefsystem.service.PublicacaoService;
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
    public String index(Model model) {
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
    public String publicacoes(Model model) {
        List<Publicacao> publicadas = publicacaoService.listarPublicadas();

        model.addAttribute("ativa", "publicacoes");
        model.addAttribute("tipos", Publicacao.TipoConteudo.values());
        model.addAttribute("publicacoes", publicadas);
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
    public Map<String, Object> publicacaoJson(@PathVariable Integer id) {
        return publicacaoService.montarDadosJson(id);
    }
}
