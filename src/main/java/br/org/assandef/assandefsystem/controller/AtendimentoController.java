package br.org.assandef.assandefsystem.controller;

import br.org.assandef.assandefsystem.model.*;
import br.org.assandef.assandefsystem.repository.FuncionarioRepository;
import br.org.assandef.assandefsystem.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/atendimento")
@RequiredArgsConstructor
public class AtendimentoController {

    private final AtendimentoService atendimentoService;
    private final PacienteService pacienteService;
    private final FuncionarioService funcionarioService;
    private final FuncionarioRepository funcionarioRepository;
    private final EvolucaoService evolucaoService;
    private final PrescricaoService prescricaoService;

    @GetMapping
    public String paginaAtendimento(
            @RequestParam(name = "pageAtendimentos", defaultValue = "0") int pageAtendimentos,
            @RequestParam(name = "sizeAtendimentos", defaultValue = "10") int sizeAtendimentos,
            Model model,
            @ModelAttribute("msg") String msg,
            @ModelAttribute("erro") String erro) {

        Page<Atendimento> atendimentosPage = atendimentoService.findAll(
                PageRequest.of(pageAtendimentos, sizeAtendimentos, Sort.by(Sort.Direction.DESC, "idAtendimento"))
        );
        List<Paciente> pacientes = pacienteService.findAll();
        List<Funcionario> funcionarios = funcionarioService.findAll();

        model.addAttribute("atendimentos", atendimentosPage.getContent());
        model.addAttribute("atendimentosPage", atendimentosPage);
        model.addAttribute("pacientes", pacientes);
        model.addAttribute("funcionarios", funcionarios);
        model.addAttribute("atendimento", new Atendimento());

        if (msg != null && !msg.isBlank()) model.addAttribute("msg", msg);
        if (erro != null && !erro.isBlank()) model.addAttribute("erro", erro);

        return "atendimento/atendimento";
    }

    @PostMapping("/iniciar")
    public String iniciarAtendimento(
            @RequestParam Integer idPaciente,
            @RequestParam(required = false) String tipoEncaminhamento,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes ra) {
        try {
            String loginUsuario = userDetails.getUsername();
            Funcionario funcionario = funcionarioRepository.findByLogin(loginUsuario)
                    .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

            Paciente paciente = pacienteService.findById(idPaciente);

            boolean temAtendimentoEmAndamento = atendimentoService.existsEmAndamentoByPaciente(idPaciente);

            if (temAtendimentoEmAndamento) {
                ra.addFlashAttribute("erro", "Este paciente já possui um atendimento em andamento. Finalize o atendimento anterior antes de iniciar um novo.");
                return "redirect:/atendimento";
            }

            Atendimento atendimento = new Atendimento();
            atendimento.setPaciente(paciente);
            atendimento.setFuncionario(funcionario);
            atendimento.setDataHoraInicio(LocalDateTime.now());
            atendimento.setStatus("EM_ANDAMENTO");
            atendimento.setTipoEncaminhamento(tipoEncaminhamento);

            atendimentoService.save(atendimento);
            ra.addFlashAttribute("msg", "Atendimento iniciado com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao iniciar atendimento: " + e.getMessage());
        }
        return "redirect:/atendimento";
    }

    @PostMapping("/{id}/finalizar")
    public String finalizarAtendimento(
            @PathVariable Integer id,
            RedirectAttributes ra) {
        try {
            Atendimento atendimento = atendimentoService.findById(id);

            // Verifica se há pelo menos uma evolução registrada
            if (atendimento.getEvolucoes() == null || atendimento.getEvolucoes().isEmpty()) {
                ra.addFlashAttribute("erro", "Não é possível finalizar o atendimento sem registrar pelo menos uma evolução.");
                return "redirect:/atendimento";
            }

            LocalDateTime agora = LocalDateTime.now();
            atendimento.setDataHoraFim(agora);
            atendimento.setDataFinalAtendimento(agora);
            atendimento.setStatus("FINALIZADO");
            atendimentoService.save(atendimento);
            ra.addFlashAttribute("msg", "Atendimento finalizado com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao finalizar atendimento: " + e.getMessage());
        }
        return "redirect:/atendimento";
    }

    @PostMapping("/{id}/evolucao")
    public String adicionarEvolucao(
            @PathVariable Integer id,
            @RequestParam String descricao,
            RedirectAttributes ra) {
        try {
            Atendimento atendimento = atendimentoService.findById(id);

            Evolucao evolucao = new Evolucao();
            evolucao.setAtendimento(atendimento);
            evolucao.setDescricao(descricao);
            evolucao.setDataHoraRegistro(LocalDateTime.now());

            evolucaoService.save(evolucao);
            ra.addFlashAttribute("msg", "Evolução adicionada com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao adicionar evolução: " + e.getMessage());
        }
        return "redirect:/atendimento";
    }

    @PostMapping("/evolucao/{idEvolucao}/prescricao")
    public String adicionarPrescricao(
            @PathVariable Integer idEvolucao,
            @RequestParam String tipo,
            @RequestParam String descricao,
            RedirectAttributes ra) {
        try {
            Evolucao evolucao = evolucaoService.findById(idEvolucao);

            Prescricao prescricao = new Prescricao();
            prescricao.setEvolucao(evolucao);
            prescricao.setTipo(tipo);
            prescricao.setDescricao(descricao);

            prescricaoService.save(prescricao);
            ra.addFlashAttribute("msg", "Prescrição adicionada com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao adicionar prescrição: " + e.getMessage());
        }
        return "redirect:/atendimento";
    }

    @PostMapping("/{id}/deletar")
    public String deletarAtendimento(
            @PathVariable Integer id,
            RedirectAttributes ra) {
        try {
            atendimentoService.deleteById(id);
            ra.addFlashAttribute("msg", "Atendimento excluído com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao excluir atendimento: " + e.getMessage());
        }
        return "redirect:/atendimento";
    }

    // API JSON para buscar atendimento com evoluções
    @ResponseBody
    @GetMapping("/{id}/detalhes")
    public ResponseEntity<br.org.assandef.assandefsystem.dto.AtendimentoDetalhesDTO> obterDetalhesAtendimento(@PathVariable Integer id) {
        try {
            Atendimento atendimento = atendimentoService.findById(id);
            br.org.assandef.assandefsystem.dto.AtendimentoDetalhesDTO dto = br.org.assandef.assandefsystem.dto.AtendimentoDetalhesDTO.from(atendimento);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @ResponseBody
    @GetMapping("/{id}")
    public ResponseEntity<Atendimento> obterAtendimento(@PathVariable Integer id) {
        try {
            Atendimento atendimento = atendimentoService.findById(id);
            return ResponseEntity.ok(atendimento);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}

