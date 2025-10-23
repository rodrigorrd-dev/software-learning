package com.academic.softwarelearning.domain.model

data class Atividade(
    val id: String  = "",
    val responsavel: String  = "",
    val tipoarquivo: String  = "",
    val titulo: String = "",
    val descricao: String = "",
    val arquivos: List<String> = listOf(),
    val dataInicio: Long = 0L,
    val dataFim: Long = 0L,
    val status: String = "Atribuída"
)
