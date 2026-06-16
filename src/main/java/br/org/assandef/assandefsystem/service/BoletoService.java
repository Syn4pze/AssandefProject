package br.org.assandef.assandefsystem.service;

import br.org.assandef.assandefsystem.model.Boleto;
import br.org.assandef.assandefsystem.model.StatusBoleto;
import br.org.assandef.assandefsystem.repository.BoletoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoletoService {
    private final BoletoRepository boletoRepository;

    public List<Boleto> findAll() {
        return boletoRepository.findAll();
    }

    public Page<Boleto> findAll(Pageable pageable) {
        return boletoRepository.findAll(pageable);
    }

    public Boleto findById(Integer id) {
        return boletoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Boleto não encontrado"));
    }

    public Boleto save(Boleto boleto) {
        return boletoRepository.save(boleto);
    }

    public void deleteById(Integer id) {
        boletoRepository.deleteById(id);
    }

    public List<Boleto> findByDoador(Integer idDoador) {
        return boletoRepository.findByDoadorIdDoador(idDoador);
    }

    public List<Boleto> findByStatus(StatusBoleto status) {
        return boletoRepository.findByStatus(status);
    }
}