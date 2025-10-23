package com.academic.softwarelearning.domain.repository

import com.academic.softwarelearning.domain.model.Arquivo

interface ArquivoRepository {
    suspend fun buscarArquivos(responsavel: String, codigoAtividade: String): List<Arquivo>
}