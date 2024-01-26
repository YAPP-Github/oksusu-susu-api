package com.oksusu.susu.post.infrastructure.repository.model

import com.oksusu.susu.extension.encodeBase64
import org.springframework.data.domain.KeysetScrollPosition
import org.springframework.data.domain.ScrollPosition
import org.springframework.data.domain.ScrollPosition.Direction
import java.time.LocalDateTime

class AllVoteCursorModel(
    val id: Long,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun toBase64Model(scrollPosition: KeysetScrollPosition): String {
            return AllVoteCursorModel(
                id = scrollPosition.keys["id"].toString().toLong(),
                createdAt = LocalDateTime.parse(scrollPosition.keys["createdAt"].toString())
            ).encodeBase64()
        }
    }

    fun toKeysetScrollPosition(direction: Direction): KeysetScrollPosition {
        return ScrollPosition.of(mapOf("id" to this.id, "createdAt" to this.createdAt), direction)
    }
}
