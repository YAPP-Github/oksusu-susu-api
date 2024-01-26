package com.oksusu.susu.post.infrastructure.repository.model

import org.springframework.data.domain.KeysetScrollPosition

class GetAllVoteSpec(
    val uid: Long,
    val searchSpec: SearchVoteSpec,
    val userBlockIds: Set<Long>,
    val postBlockIds: Set<Long>,
    val keysetScrollPosition: KeysetScrollPosition,
)
