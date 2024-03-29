package com.oksusu.susu.api.event.listener

import com.oksusu.susu.api.event.model.CacheUserEnvelopeStatisticEvent
import com.oksusu.susu.api.statistic.application.UserEnvelopeStatisticService
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class StatisticEventListener(
    private val userEnvelopeStatisticService: UserEnvelopeStatisticService,
) {
    val logger = KotlinLogging.logger { }

    @EventListener
    fun cacheUserEnvelopStatistic(event: CacheUserEnvelopeStatisticEvent) {
        CoroutineScope(Dispatchers.IO + Job()).launch {
            logger.info { "[${event.publishAt}] ${event.uid} 유저 봉투 통계 캐싱 시작" }

            userEnvelopeStatisticService.save(event.uid, event.statistic)

            logger.info { "[${event.publishAt}] ${event.uid} 유저 봉투 통계 캐싱 끝" }
        }
    }
}
