package com.oscar.orbimusic.data.network.dto

// Envoltorio de la response de deezer
data class DeezerListDto<T>(
    val data: List<T>?,
    val total: Int?,
    val next: String?
)
