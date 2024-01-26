package com.oksusu.susu.envelope.infrastructure.model

data class SearchFriendStatisticsSpec(
    val uid: Long,
    val friendIds: Set<Long>?,
    val fromTotalAmounts: Long?,
    val toTotalAmounts: Long?,
)
