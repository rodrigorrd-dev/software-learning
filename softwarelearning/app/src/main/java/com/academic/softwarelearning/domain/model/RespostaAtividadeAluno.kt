package com.academic.softwarelearning.domain.model

data class RespostaAtividadeAluno(
    val atividadeId: String = "",
    val texto: String = "",
    val usuarioId: String = "",
    val integracaoIA: String? = null,
)
