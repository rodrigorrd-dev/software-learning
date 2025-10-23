package com.api.softwarelearningapi.api.v1.model;

import lombok.Data;

import java.time.OffsetDateTime;

public class ArquivoModel {

    private Long id;

    private String codigoatividadefirebase;

    private String nome;

    private byte[] arquivo;

    private OffsetDateTime dataCriacao;

    private OffsetDateTime dataAtualizacao;

    private String responsavel;

    private String tipoarquivo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoatividadefirebase() {
        return codigoatividadefirebase;
    }

    public void setCodigoatividadefirebase(String codigoatividadefirebase) {
        this.codigoatividadefirebase = codigoatividadefirebase;
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

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    public String getTipoarquivo() {
        return tipoarquivo;
    }

    public void setTipoarquivo(String tipoarquivo) {
        this.tipoarquivo = tipoarquivo;
    }
}
