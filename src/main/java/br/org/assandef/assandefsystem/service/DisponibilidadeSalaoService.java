package br.org.assandef.assandefsystem.service;

import br.org.assandef.assandefsystem.model.DisponibilidadeSalao;
import br.org.assandef.assandefsystem.model.DisponibilidadeSalao.StatusDisponibilidadeSalao;
import br.org.assandef.assandefsystem.repository.DisponibilidadeSalaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DisponibilidadeSalaoService {

    private final DisponibilidadeSalaoRepository disponibilidadeSalaoRepository;

    public List<DisponibilidadeSalao> findAll() {
        return disponibilidadeSalaoRepository.findAllByOrderByDataLocacaoAscHoraInicioAsc();
    }

    public List<DisponibilidadeSalao> findDisponiveisParaPaginaPublica() {
        return disponibilidadeSalaoRepository
                .findByStatusAndDataLocacaoGreaterThanEqualOrderByDataLocacaoAscHoraInicioAsc(
                        StatusDisponibilidadeSalao.DISPONIVEL,
                        LocalDate.now()
                );
    }

    public DisponibilidadeSalao findById(Integer id) {
        return disponibilidadeSalaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Disponibilidade não encontrada com ID: " + id));
    }

    @Transactional
    public DisponibilidadeSalao save(DisponibilidadeSalao disponibilidade) {
        validarDisponibilidade(disponibilidade);
        if (disponibilidade.getStatus() == null) {
            disponibilidade.setStatus(StatusDisponibilidadeSalao.DISPONIVEL);
        }
        return disponibilidadeSalaoRepository.save(disponibilidade);
    }

    @Transactional
    public void deleteById(Integer id) {
        if (!disponibilidadeSalaoRepository.existsById(id)) {
            throw new RuntimeException("Disponibilidade não encontrada com ID: " + id);
        }
        disponibilidadeSalaoRepository.deleteById(id);
    }

    @Transactional
    public DisponibilidadeSalao alterarStatus(Integer id, StatusDisponibilidadeSalao status) {
        DisponibilidadeSalao disponibilidade = findById(id);
        disponibilidade.setStatus(status);
        return disponibilidadeSalaoRepository.save(disponibilidade);
    }

    private void validarDisponibilidade(DisponibilidadeSalao disponibilidade) {
        if (disponibilidade.getHoraInicio() != null
                && disponibilidade.getHoraFim() != null
                && !disponibilidade.getHoraFim().isAfter(disponibilidade.getHoraInicio())) {
            throw new RuntimeException("O horário final deve ser maior que o horário inicial.");
        }

        if (disponibilidade.getValor() != null && disponibilidade.getValor().signum() < 0) {
            throw new RuntimeException("O valor da locação não pode ser negativo.");
        }
    }
}
