package com.academic.softwarelearning.domain.model

data class Arquivo(
    val id: Long,
    val nome: String,
    val arquivo: String,
    val tipoarquivo: String
)