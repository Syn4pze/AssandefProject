package br.org.assandef.assandefsystem.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "publicacoes_videos")
@Data
public class PublicacaoVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idVideo;

    @ManyToOne
    @JoinColumn(name = "id_publicacao", nullable = false)
    private Publicacao publicacao;

    @Column(length = 500, nullable = false)
    private String urlYoutube;

    @Column(length = 180)
    private String tituloVideo;

    @Column(nullable = false)
    private Short ordemExibicao = 1;

    private LocalDateTime dataCadastro;
}