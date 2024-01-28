package com.oksusu.susu.event.model

import com.oksusu.susu.ledger.domain.Ledger
import com.oksusu.susu.term.domain.TermAgreement
import com.oksusu.susu.term.domain.vo.TermAgreementChangeType
import com.oksusu.susu.user.domain.UserDevice
import java.time.LocalDateTime

sealed interface Event

open class BaseEvent(
    val publishAt: LocalDateTime = LocalDateTime.now(),
) : Event

data class DeleteLedgerEvent(
    val ledger: Ledger,
) : BaseEvent()

data class TermAgreementHistoryCreateEvent(
    val termAgreements: List<TermAgreement>,
    val changeType: TermAgreementChangeType,
) : BaseEvent()

data class CreateUserDeviceEvent(
    val userDevice: UserDevice,
) : BaseEvent()

data class UpdateUserDeviceEvent(
    val userDevice: UserDevice,
) : BaseEvent()
