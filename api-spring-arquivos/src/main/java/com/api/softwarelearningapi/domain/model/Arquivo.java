package com.api.softwarelearningapi.domain.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "arquivo")
public class Arquivo {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigoatividadefirebase", columnDefinition = "VARCHAR(255)")
    private String codigoAtividadeFirebase;

    @Column(name = "nome", columnDefinition = "VARCHAR(255)")
    private String nome;

    @Column(name = "conteudo")
    private byte[] arquivo;

    @Column(name = "responsavel", columnDefinition = "VARCHAR(255)")
    private String responsavel;

    @Column(name = "tipoarquivo", columnDefinition = "VARCHAR(255)")
    private String tipoarquivo;

    @Column(name = "datacriacao", columnDefinition = "VARCHAR(255)")
    private OffsetDateTime dataCriacao;

    @Column(name = "dataatualizacao", columnDefinition = "VARCHAR(255)")
    private OffsetDateTime dataAtualizacao;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoAtividadeFirebase() {
        return codigoAtividadeFirebase;
    }

    public void setCodigoAtividadeFirebase(String codigoatividadefirebase) {
        this.codigoAtividadeFirebase = codigoatividadefirebase;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public byte[] getArquivo() {
        return arquivo;
    }

    public void setArquivo(byte[] arquivo) {
        this.arquivo = arquivo;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    public OffsetDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(OffsetDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public OffsetDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(OffsetDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public String getTipoarquivo() {
        return tipoarquivo;
    }

    public void setTipoarquivo(String tipoarquivo) {
        this.tipoarquivo = tipoarquivo;
    }
}
