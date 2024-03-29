package com.oksusu.susu.domain.cache

import com.fasterxml.jackson.core.type.TypeReference
import com.oksusu.susu.common.consts.SUSU_ENVELOPE_STATISTIC_KEY
import com.oksusu.susu.common.consts.SUSU_STATISTIC_TTL
import com.oksusu.susu.common.consts.USER_STATISTIC_TTL
import com.oksusu.susu.common.util.toTypeReference
import com.oksusu.susu.domain.statistic.domain.SusuEnvelopeStatistic
import com.oksusu.susu.domain.statistic.domain.UserEnvelopeStatistic
import java.time.Duration

class Cache<VALUE_TYPE>(
    val key: String,
    val type: TypeReference<VALUE_TYPE>,
    val duration: Duration,
) {
    companion object Factory {
        fun getSusuEnvelopeStatisticCache(): Cache<SusuEnvelopeStatistic> {
            return Cache(
                key = SUSU_ENVELOPE_STATISTIC_KEY,
                type = toTypeReference(),
                duration = Duration.ofSeconds(SUSU_STATISTIC_TTL)
            )
        }

        val getSusuEnvelopeStatisticCache: Factory.() -> Cache<SusuEnvelopeStatistic> =
            { getSusuEnvelopeStatisticCache() }

        fun getSusuSpecificStatisticCache(key: String): Cache<Long> {
            return Cache(
                key = key,
                type = toTypeReference(),
                duration = Duration.ofSeconds(SUSU_STATISTIC_TTL)
            )
        }

        fun getUserEnvelopeStatisticCache(key: String): Cache<UserEnvelopeStatistic> {
            return Cache(
                key = key,
                type = toTypeReference(),
                duration = Duration.ofSeconds(USER_STATISTIC_TTL)
            )
        }

        fun getRefreshTokenCache(key: String, ttl: Long): Cache<String> {
            return Cache(
                key = key,
                type = toTypeReference(),
                duration = Duration.ofSeconds(ttl)
            )
        }
    }
}
