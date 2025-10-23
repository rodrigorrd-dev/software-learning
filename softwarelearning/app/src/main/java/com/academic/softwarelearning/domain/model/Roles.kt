package com.academic.softwarelearning.domain.model

object Roles {
    const val ADMIN = "ADMIN"
    const val DIRETOR = "DIRETOR"
    const val ORIENTADOR = "ORIENTADOR"
    const val ALUNO = "ALUNO"
}
data class UserClaims(val role: String?)
