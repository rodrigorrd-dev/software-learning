package com.api.softwarelearningapi.api.v1.model.input;

import com.api.softwarelearningapi.core.validation.FileContentType;
import com.api.softwarelearningapi.core.validation.FileSize;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import org.springframework.http.MediaType;

public class ArquivoInput {

    private String codigoatividadefirebase;

    private String nome;

    @JsonIgnore
    @FileSize(max = "100000KB")
    @FileContentType(allowed = { MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.MULTIPART_MIXED_VALUE })
    private byte[] arquivo;

    private String responsavel;

    private String tipoarquivo;

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

    @FileSize(max = "100000KB")
    @FileContentType(allowed = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.MULTIPART_MIXED_VALUE})
    public byte[] getArquivo() {
        return arquivo;
    }

    public void setArquivo(@FileSize(max = "10000KB") @FileContentType(allowed = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.MULTIPART_MIXED_VALUE}) byte[] arquivo) {
        this.arquivo = arquivo;
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
