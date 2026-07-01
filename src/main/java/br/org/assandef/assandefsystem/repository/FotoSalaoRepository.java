package br.org.assandef.assandefsystem.repository;

import br.org.assandef.assandefsystem.model.FotoSalao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FotoSalaoRepository extends JpaRepository<FotoSalao, Integer> {

    List<FotoSalao> findAllByOrderByAtivoDescFotoPrincipalDescOrdemExibicaoAscDataUploadDesc();

    List<FotoSalao> findByAtivoTrueOrderByFotoPrincipalDescOrdemExibicaoAscDataUploadDesc();

    Optional<FotoSalao> findFirstByAtivoTrueAndFotoPrincipalTrueOrderByOrdemExibicaoAsc();
}
