package br.org.assandef.assandefsystem.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "publicacoes_imagens")
@Data
public class PublicacaoImagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idImagem;

    @ManyToOne
    @JoinColumn(name = "id_publicacao", nullable = false)
    private Publicacao publicacao;

    @Column(length = 500, nullable = false)
    private String caminhoArquivo;

    @Column(length = 255)
    private String nomeOriginal;

    @Column(length = 50, nullable = false)
    private String tipoMime;

    @Column(nullable = false)
    private Long tamanhoBytes;

    @Column(nullable = false)
    private Short ordemExibicao = 1;

    @Column(nullable = false)
    private Boolean imagemPrincipal = false;

    @Column(length = 255)
    private String textoAlternativo;

    private LocalDateTime dataUpload;
}