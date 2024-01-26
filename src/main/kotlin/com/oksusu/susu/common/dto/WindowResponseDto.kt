package com.oksusu.susu.common.dto

import org.springframework.data.domain.Window

data class WindowResponseDto<T>(
    val data: List<T>,
    val size: Int?,
    val isLast: Boolean,
    val cursor: String?,
) {
    constructor(pair: Pair<Window<T>, String?>) : this(
        data = pair.first.content,
        size = pair.first.content.size,
        isLast = pair.first.isLast,
        cursor = pair.second
    )
}
