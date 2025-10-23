package com.api.softwarelearningapi.domain.service;

import com.api.softwarelearningapi.domain.exception.ArquivoNaoEncontradoException;
import com.api.softwarelearningapi.domain.model.Arquivo;
import com.api.softwarelearningapi.domain.repository.ArquivoRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ArquivoService {

    @Autowired
    private ArquivoRepository arquivoRepository;

    @Transactional
    public Arquivo salvarTurma(Arquivo arquivo) {
        //log.info("Adicionando arquivo na data de " + LocalDateTime.now() + " arquivo " + arquivo.getNome());
        return arquivoRepository.save(arquivo);
    }

    public Arquivo buscarOuFalhar(Long arquivoId) {
        return arquivoRepository.findById(arquivoId)
                .orElseThrow(() -> new ArquivoNaoEncontradoException("Arquivo Não Encontrado."));
    }

    public List<Arquivo> buscarOuFalharArquivoResponsavel(String responsavel, String codigoatividadefirebase) {
        return arquivoRepository.findByResponsavelAndCodigoAtividadeFirebase(responsavel, codigoatividadefirebase);
    }

    public void deletar(String responsavel, String codigoatividadefirebase) {
        //log.info("Responsavel pode delete: " + responsavel.toString());
        List<Arquivo> arquivo = buscarOuFalharArquivoResponsavel(responsavel, codigoatividadefirebase);
        arquivoRepository.deleteAll(arquivo);
    }
}
