package br.org.assandef.assandefsystem.controller;

import br.org.assandef.assandefsystem.service.PublicacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import br.org.assandef.assandefsystem.model.Publicacao;
import br.org.assandef.assandefsystem.model.PublicacaoVideo;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import org.springframework.ui.Model;
import br.org.assandef.assandefsystem.model.PublicacaoImagem;
import java.util.Map;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class WebController {
    private final PublicacaoService publicacaoService;
    @GetMapping("/")
    public String index(Model model) {
        List<Publicacao> ultimasPublicacoes = publicacaoService.listarTodas().stream()
                .sorted(java.util.Comparator
                        .comparing(Publicacao::getDataCriacao,
                                java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))
                        .reversed())
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
        return "home/sobre"; // era "sobre", agora aponta para a subpasta
    }
    @GetMapping("/publicacoes")
    public String publicacoes(Model model) {
        List<Publicacao> todas = publicacaoService.listarTodas();

        // mapa id → lista de caminhos de imagem
        Map<Integer, List<String>> imagensPorPublicacao = todas.stream()
                .collect(java.util.stream.Collectors.toMap(
                        Publicacao::getIdPublicacao,
                        p -> p.getImagens() == null ? List.of() :
                                p.getImagens().stream()
                                        .map(PublicacaoImagem::getCaminhoArquivo)
                                        .collect(java.util.stream.Collectors.toList())
                ));

        try {
            model.addAttribute("imagensPorPublicacaoJson",
                    new com.fasterxml.jackson.databind.ObjectMapper()
                            .writeValueAsString(imagensPorPublicacao));
        } catch (Exception e) {
            model.addAttribute("imagensPorPublicacaoJson", "{}");
        }

        model.addAttribute("ativa", "publicacoes");
        model.addAttribute("tipos", Publicacao.TipoConteudo.values());
        model.addAttribute("publicacoes", todas);
        model.addAttribute("proximosEventos", todas.stream()
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
                ? pub.getDataEvento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH'h'mm"))
                : null);
        map.put("dataEventoIso", pub.getDataEvento() != null
                ? pub.getDataEvento().toString()
                : null);
        return map;
    }

}
