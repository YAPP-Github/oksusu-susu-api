package com.oksusu.susu.auth.domain

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import org.springframework.data.redis.core.index.Indexed

@RedisHash(value = "refreshToken")
class RefreshToken(
    @Id
    var id: Long,

    @Indexed
    var refreshToken: String,

    @TimeToLive // TTL
    var ttl: Int,
)
