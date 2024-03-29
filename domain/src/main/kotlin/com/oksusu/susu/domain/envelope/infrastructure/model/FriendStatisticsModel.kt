package com.oksusu.susu.domain.envelope.infrastructure.model

import com.querydsl.core.annotations.QueryProjection

data class FriendStatisticsModel @QueryProjection constructor(
    val friendId: Long,
    val sentAmounts: Long,
    val receivedAmounts: Long,
)
