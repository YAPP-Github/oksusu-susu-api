package com.oksusu.susu.api.event.listener

import com.oksusu.susu.api.event.model.CreateUserWithdrawEvent
import com.oksusu.susu.api.user.application.UserWithdrawService
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class UserWithdrawEventListener(
    private val userWithdrawService: UserWithdrawService,
) {
    val logger = KotlinLogging.logger { }

    @TransactionalEventListener
    fun createUserWithdrawService(event: CreateUserWithdrawEvent) {
        CoroutineScope(Dispatchers.IO + Job()).launch {
            val userWithdraw = event.userWithdraw

            logger.info { "[${event.publishAt}] ${userWithdraw.uid} 유저 탈퇴 엔티티 저장 시작" }

            userWithdrawService.saveSync(userWithdraw)

            logger.info { "[${event.publishAt}] ${userWithdraw.uid} 유저 탈퇴 엔티티 저장 끝" }
        }
    }
}
