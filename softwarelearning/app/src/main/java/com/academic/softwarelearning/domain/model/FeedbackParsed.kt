package com.academic.softwarelearning.domain.model

data class FeedbackParsed(
    val percentual: Int,
    val strengths: List<String>,
    val improvements: List<String>,
    val observations: String
)
