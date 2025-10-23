package com.api.softwarelearningapi.api.v1.assembler;

import com.api.softwarelearningapi.api.v1.model.ArquivoModel;
import com.api.softwarelearningapi.domain.model.Arquivo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;

@Component
public class ArquivoModelAssembler {

    @Autowired
    private ModelMapper modelMapper;

    public ArquivoModel toModel(Arquivo arquivo) {
        return modelMapper.map(arquivo, ArquivoModel.class);
    }

    public ArquivoModel toModelResumo(Arquivo arquivo) {
        return modelMapper.map(arquivo, ArquivoModel.class);
    }

    public List<ArquivoModel> toCollectionModel(Collection<Arquivo> arquivos) {
        return arquivos.stream()
                .map(arquivo -> toModel(arquivo))
                .collect(Collectors.toList());
    }

    public List<ArquivoModel> toCollectionModelResumo(Collection<Arquivo> arquivos) {
        return arquivos.stream()
                .map(arquivo -> toModelResumo(arquivo))
                .collect(Collectors.toList());
    }
}
