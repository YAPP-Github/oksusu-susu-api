package com.oksusu.susu.health.dto

import java.time.LocalDateTime

data class HealthResponse(
    val message: String,
    val dateTime: LocalDateTime,
    val profile: String,
)
