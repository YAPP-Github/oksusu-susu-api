package com.oksusu.susu.term.model

data class TermModel(
    val id: Long,
    val title: String,
    val description: String,
    val isEssential: Boolean,
    val isActive: Boolean,
)
