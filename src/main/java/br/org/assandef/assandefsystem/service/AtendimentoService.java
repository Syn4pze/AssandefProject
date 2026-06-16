package br.org.assandef.assandefsystem.service;

import br.org.assandef.assandefsystem.model.Atendimento;
import br.org.assandef.assandefsystem.repository.AtendimentoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AtendimentoService {
    private final AtendimentoRepository atendimentoRepository;

    public AtendimentoService(AtendimentoRepository atendimentoRepository) {
        this.atendimentoRepository = atendimentoRepository;
    }

    public List<Atendimento> findAll() {
        return atendimentoRepository.findAll();
    }

    public Page<Atendimento> findAll(Pageable pageable) {
        return atendimentoRepository.findAll(pageable);
    }

    public Atendimento findById(Integer id) {
        return atendimentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Atendimento não encontrado: " + id));
    }

    public Atendimento save(Atendimento a) {
        return atendimentoRepository.save(a);
    }

    public void deleteById(Integer id) {
        atendimentoRepository.deleteById(id);
    }

    public boolean existsEmAndamentoByPaciente(Integer idPaciente) {
        return atendimentoRepository.existsByPaciente_IdPacienteAndStatus(idPaciente, "EM_ANDAMENTO");
    }

    public List<Atendimento> findByDataHoraInicioBetween(LocalDateTime inicio, LocalDateTime fim) {
        return atendimentoRepository.findByDataHoraInicioBetween(inicio, fim);
    }
}