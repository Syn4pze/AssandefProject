package br.org.assandef.assandefsystem.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "publicacoes")
@Data
public class Publicacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPublicacao;

    @ManyToOne
    @JoinColumn(name = "id_funcionario_autor")
    private Funcionario funcionarioAutor;

    @Column(length = 180, nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descricao;

    @Column(columnDefinition = "LONGTEXT")
    private String conteudo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_conteudo", nullable = false)
    private TipoConteudo tipoConteudo;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    private LocalDateTime dataPublicacao;
    private LocalDateTime dataEvento;

    @Column(length = 255)
    private String localEvento;

    @OneToMany(mappedBy = "publicacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PublicacaoImagem> imagens;

    @OneToMany(mappedBy = "publicacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PublicacaoVideo> videos;

    public enum TipoConteudo { NOTICIA, EVENTO, ARTIGO }
}