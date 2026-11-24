package com.equipe02.mediastore

// Modelo leve: guardamos apenas metadados, nunca os pixels das imagens.
data class Imagem(val id: Long, val nome: String, val bytes: Long) {
    val tamanhoKB: Double get() = bytes / 1024.0
}
