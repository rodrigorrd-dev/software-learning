package com.academic.softwarelearning.infrastructure.repository

import com.academic.softwarelearning.domain.repository.ArquivoRepository
import com.academic.softwarelearning.infrastructure.network.ApiService

class ArquivoRepositoryImpl(private val apiService: ApiService) : ArquivoRepository {
    override
    suspend fun buscarArquivos(responsavel: String, codigoAtividade: String) =
        apiService.buscarArquivos(responsavel, codigoAtividade)
}