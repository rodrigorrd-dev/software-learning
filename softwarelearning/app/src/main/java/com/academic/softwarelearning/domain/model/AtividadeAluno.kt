package com.academic.softwarelearning.domain.model

data class AtividadeAluno(
    val id: String? = "",
    val responsavel: String? = "",
    val tipoarquivo: String? = "",
    val titulo: String = "",
    val descricao: String = "",
    val arquivos: List<String> = listOf(), // Lista de arquivos
    val dataInicio: Long = 0L, // Timestamp da data de início
    val dataFim: Long = 0L, // Timestamp da data de fim
    val status: String = "Atribuída", // Status da atividade
)