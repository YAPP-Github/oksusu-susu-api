package com.oksusu.susu.post.infrastructure.repository.model

import com.oksusu.susu.extension.encodeBase64
import org.springframework.data.domain.KeysetScrollPosition
import org.springframework.data.domain.ScrollPosition
import org.springframework.data.domain.ScrollPosition.Direction
import java.time.LocalDateTime

class PopularVoteCursorModel(
    val id: Long,
    val createdAt: LocalDateTime,
    val count: Long,
) {
    companion object {
        fun toBase64Model(scrollPosition: KeysetScrollPosition): String {
            println(scrollPosition.toString())
            return PopularVoteCursorModel(
                id = scrollPosition.keys["id"].toString().toLong(),
                createdAt = LocalDateTime.parse(scrollPosition.keys["createdAt"].toString()),
                count = scrollPosition.keys["count"].toString().toLong(),
            ).encodeBase64()
        }
    }

    fun toKeysetScrollPosition(direction: Direction): KeysetScrollPosition {
        return ScrollPosition.of(
            mapOf("id" to this.id, "createdAt" to this.createdAt, "count" to this.count),
            direction
        )
    }
}
