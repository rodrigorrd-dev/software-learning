package com.api.softwarelearningapi.api.v1.controller;

import com.api.softwarelearningapi.api.v1.assembler.ArquivoModelAssembler;
import com.api.softwarelearningapi.api.v1.assembler.disassembler.ArquivoInputDisassembler;
import com.api.softwarelearningapi.api.v1.model.ArquivoModel;
import com.api.softwarelearningapi.api.v1.model.input.ArquivoInput;
import com.api.softwarelearningapi.domain.model.Arquivo;
import com.api.softwarelearningapi.domain.service.ArquivoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/v1/arquivos", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
public class ArquivoController {

    @Autowired
    private ArquivoService arquivoService;

    @Autowired
    private ArquivoModelAssembler arquivoModelAssembler;

    @Autowired
    private ArquivoInputDisassembler arquivoInputDisassembler;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ArquivoModel adicionarArquivo(
            @RequestParam("codigoatividadefirebase") String codigoatividadefirebase,
            @RequestParam("nome") String nome,
            @RequestParam("responsavel") String responsavel,
            @RequestParam("tipoarquivo") String tipoarquivo,
            @RequestPart("arquivo") MultipartFile arquivo
    ) throws IOException {
        ArquivoInput arquivoInput = new ArquivoInput();
        arquivoInput.setCodigoatividadefirebase(codigoatividadefirebase);
        arquivoInput.setNome(nome);
        arquivoInput.setResponsavel(responsavel);
        arquivoInput.setTipoarquivo(tipoarquivo);
        arquivoInput.setArquivo(arquivo.getBytes());

        Arquivo entidade = arquivoInputDisassembler.toDomainObject(arquivoInput);
        Arquivo salvo = arquivoService.salvarTurma(entidade);
        return arquivoModelAssembler.toModel(salvo);
    }

    @GetMapping("/{responsavel}/{codigoatividadefirebase}")
    public List<ArquivoModel> buscar(@PathVariable String responsavel, @PathVariable String codigoatividadefirebase) {
        List<Arquivo> arquivo = arquivoService.buscarOuFalharArquivoResponsavel(responsavel, codigoatividadefirebase);
        return arquivoModelAssembler.toCollectionModelResumo(arquivo);
    }

    @DeleteMapping("/{responsavel}/{codigoatividadefirebase}")
    public ResponseEntity<Arquivo> deletar(@PathVariable String responsavel, @PathVariable String codigoatividadefirebase) {
        arquivoService.deletar(responsavel, codigoatividadefirebase);
        return ResponseEntity.ok().build();
    }
}
