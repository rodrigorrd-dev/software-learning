package com.api.softwarelearningapi.domain.repository;

import com.api.softwarelearningapi.domain.model.Arquivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArquivoRepository extends JpaRepository<Arquivo, Long> {
    List<Arquivo> findByResponsavelAndCodigoAtividadeFirebase(
            String responsavel,
            String codigoatividadefirebase
    );
}
