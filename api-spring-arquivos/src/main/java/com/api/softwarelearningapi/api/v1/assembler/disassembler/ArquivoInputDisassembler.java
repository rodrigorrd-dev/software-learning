package com.api.softwarelearningapi.api.v1.assembler.disassembler;

import com.api.softwarelearningapi.api.v1.model.input.ArquivoInput;
import com.api.softwarelearningapi.domain.model.Arquivo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ArquivoInputDisassembler {

    @Autowired
    private ModelMapper modelMapper;

    public Arquivo toDomainObject(ArquivoInput arquivoInput) {
        return modelMapper.map(arquivoInput, Arquivo.class);
    }

    public void copyToDomainObject(ArquivoInput arquivoInput, Arquivo arquivo) {
        modelMapper.map(arquivoInput, arquivo);
    }

}
