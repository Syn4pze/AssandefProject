package br.org.assandef.assandefsystem.service;

import br.org.assandef.assandefsystem.model.Doador;
import br.org.assandef.assandefsystem.repository.DoadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoadorService {
    private final DoadorRepository doadorRepository;

    // ========= Normalização =========
    private String onlyDigits(String s) {
        return (s == null) ? null : s.replaceAll("\\D", "");
    }

    private String normalizeEmail(String e) {
        return (e == null) ? null : e.trim().toLowerCase();
    }

    private void normalizeFields(Doador d) {
        if (d == null) return;
        if (d.getCpfCnpj() != null) d.setCpfCnpj(onlyDigits(d.getCpfCnpj()));
        if (d.getTelefone() != null) d.setTelefone(onlyDigits(d.getTelefone()));
        if (d.getEmail() != null) d.setEmail(normalizeEmail(d.getEmail()));
    }

    // ========= Consultas básicas =========
    public List<Doador> findAll() {
        return doadorRepository.findAll();
    }

    public Page<Doador> findAll(Pageable pageable) {
        return doadorRepository.findAll(pageable);
    }

    public Doador findById(Integer id) {
        return doadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doador não encontrado"));
    }

    public void deleteById(Integer id) {
        doadorRepository.deleteById(id);
    }

    public Doador findByCpfCnpj(String cpfCnpj) {
        String c = onlyDigits(cpfCnpj);
        return doadorRepository.findByCpfCnpj(c)
                .orElseThrow(() -> new RuntimeException("Doador não encontrado"));
    }

    // ========= Verificação de unicidade (criação vs edição) =========
    public boolean existsByCpfCnpjOrEmailOrTelefoneExcludingId(String cpfCnpj, String email, String telefone, Integer idDoador) {
        String c = StringUtils.hasText(cpfCnpj) ? onlyDigits(cpfCnpj) : null;
        String e = StringUtils.hasText(email) ? normalizeEmail(email) : null;
        String t = StringUtils.hasText(telefone) ? onlyDigits(telefone) : null;

        if (idDoador == null) {
            // criação: verifica existência global
            if (c != null && doadorRepository.existsByCpfCnpj(c)) return true;
            if (e != null && doadorRepository.existsByEmailIgnoreCase(e)) return true;
            if (t != null && doadorRepository.existsByTelefone(t)) return true;
            return false;
        } else {
            // edição: verifica existência excluindo o próprio id
            if (c != null && doadorRepository.existsByCpfCnpjAndIdDoadorNot(c, idDoador)) return true;
            if (e != null && doadorRepository.existsByEmailIgnoreCaseAndIdDoadorNot(e, idDoador)) return true;
            if (t != null && doadorRepository.existsByTelefoneAndIdDoadorNot(t, idDoador)) return true;
            return false;
        }
    }

    // ========= Save / Update =========
    public Doador save(Doador doador) {
        normalizeFields(doador);
        if (doador.getIdDoador() == null) {
            doador.setDataCadastro(LocalDate.now());
        }
        return doadorRepository.save(doador);
    }

    /**
     * Atualiza de forma segura: carrega o existente, copia campos mutáveis e salva.
     * Use este método no fluxo de edição (POST /editar/{id}).
     */
    public Doador update(Integer id, Doador dto) {
        Doador existing = doadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doador não encontrado"));

        // Normaliza dados do dto antes de aplicar
        normalizeFields(dto);
        // Copiar apenas campos que podem ser alterados via UI
        existing.setNome(dto.getNome());
        existing.setCpfCnpj(dto.getCpfCnpj());
        existing.setTelefone(dto.getTelefone());
        existing.setEmail(dto.getEmail());
        existing.setDataNascimento(dto.getDataNascimento());
        existing.setSexo(dto.getSexo());
        existing.setMensalidade(dto.getMensalidade());
        existing.setDiaVencimento(dto.getDiaVencimento());
        existing.setEndereco(dto.getEndereco());
        // Copie outros campos mutáveis conforme sua model

        return doadorRepository.save(existing);
    }

    public List<Doador> findByDataCadastroBetween(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null && dataFim == null) {
            return doadorRepository.findAll();
        }
        if (dataInicio == null) {
            return doadorRepository.findByDataCadastroLessThanEqual(dataFim);
        }
        if (dataFim == null) {
            return doadorRepository.findByDataCadastroGreaterThanEqual(dataInicio);
        }
        // Between é inclusivo em ambos os lados
        return doadorRepository.findByDataCadastroBetween(dataInicio, dataFim);
    }
}