package com.oksusu.susu.api.auth.application

import arrow.fx.coroutines.parZip
import com.oksusu.susu.api.auth.helper.TokenGenerateHelper
import com.oksusu.susu.api.auth.model.*
import com.oksusu.susu.api.auth.model.response.TokenRefreshRequest
import com.oksusu.susu.api.event.model.CreateUserStatusHistoryEvent
import com.oksusu.susu.api.event.model.CreateUserWithdrawEvent
import com.oksusu.susu.api.post.application.PostService
import com.oksusu.susu.api.user.application.UserService
import com.oksusu.susu.api.user.application.UserStatusService
import com.oksusu.susu.api.user.application.UserStatusTypeService
import com.oksusu.susu.common.exception.ErrorCode
import com.oksusu.susu.common.exception.InvalidTokenException
import com.oksusu.susu.common.exception.NoAuthorityException
import com.oksusu.susu.domain.auth.domain.RefreshToken
import com.oksusu.susu.domain.common.extension.coExecuteOrNull
import com.oksusu.susu.domain.config.database.TransactionTemplates
import com.oksusu.susu.domain.user.domain.UserStatusHistory
import com.oksusu.susu.domain.user.domain.UserWithdraw
import com.oksusu.susu.domain.user.domain.vo.AccountRole
import com.oksusu.susu.domain.user.domain.vo.UserStatusAssignmentType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Service
class AuthFacade(
    private val userService: UserService,
    private val jwtTokenService: JwtTokenService,
    private val refreshTokenService: RefreshTokenService,
    private val tokenGenerateHelper: TokenGenerateHelper,
    private val oAuthService: OAuthService,
    private val postService: PostService,
    private val userStatusService: UserStatusService,
    private val eventPublisher: ApplicationEventPublisher,
    private val txTemplates: TransactionTemplates,
    private val userStatusTypeService: UserStatusTypeService,
) {
    fun resolveAuthUser(token: Mono<AuthUserToken>): Mono<Any> {
        return jwtTokenService.verifyTokenMono(token)
            .map { payload ->
                if (payload.type != "accessToken") {
                    throw InvalidTokenException(ErrorCode.INVALID_ACCESS_TOKEN)
                }
                val user = userService.findByIdOrThrowSync(payload.id)

                AuthUserImpl(user.id)
            }
    }

    fun resolveAdminUser(token: Mono<AuthUserToken>): Mono<Any> {
        return jwtTokenService.verifyTokenMono(token)
            .map { payload ->
                if (payload.type != "accessToken") {
                    throw InvalidTokenException(ErrorCode.INVALID_ACCESS_TOKEN)
                }

                val user = userService.findByIdOrThrowSync(payload.id)

                if (user.role != AccountRole.ADMIN) {
                    throw NoAuthorityException(ErrorCode.NO_AUTHORITY_ERROR)
                }

                AdminUserImpl(user.id)
            }
    }

    @Transactional
    suspend fun logout(user: AuthUser) {
        refreshTokenService.deleteByKey(user.uid.toString())
    }

    @Transactional
    suspend fun refreshToken(request: TokenRefreshRequest): TokenDto {
        val accessPayload = jwtTokenService.verifyTokenWithExtendedExpiredAt(request.accessToken)
        val refreshPayload = jwtTokenService.verifyRefreshToken(request.refreshToken)

        if (accessPayload.id != refreshPayload.id) {
            throw NoAuthorityException(ErrorCode.INVALID_TOKEN)
        }

        return parZip(
            { refreshTokenService.deleteByKey(refreshPayload.id.toString()) },
            { tokenGenerateHelper.generateAccessAndRefreshToken(refreshPayload.id) }
        ) { _, tokenDto ->
            RefreshToken(
                uid = refreshPayload.id,
                refreshToken = tokenDto.refreshToken
            ).run { refreshTokenService.save(this) }

            tokenDto
        }
    }

    @Transactional
    suspend fun withdraw(authUser: AuthUser) {
        val (deactivatedPosts, userAndUserStatusModel) = parZip(
            { postService.findAllByUid(authUser.uid) },
            { userService.getUserAndUserStatus(authUser.uid) }
        ) { posts, userAndUserStatusModel ->
            val deactivatedPosts = posts.map { post -> post.apply { isActive = false } }

            deactivatedPosts to userAndUserStatusModel
        }

        val user = userAndUserStatusModel.user
        val userStatus = userAndUserStatusModel.userStatus

        val withdrawUser = UserWithdraw(
            uid = user.id,
            oauthInfo = user.oauthInfo,
            role = user.role
        )

        coroutineScope {
            val txDeferred = async {
                txTemplates.writer.coExecuteOrNull {
                    eventPublisher.publishEvent(
                        CreateUserStatusHistoryEvent(
                            userStatusHistory = UserStatusHistory(
                                uid = user.id,
                                statusAssignmentType = UserStatusAssignmentType.ACCOUNT,
                                fromStatusId = userStatus.accountStatusId,
                                toStatusId = userStatusTypeService.getDeletedStatusId()
                            )
                        )
                    )

                    eventPublisher.publishEvent(
                        CreateUserWithdrawEvent(
                            userWithdraw = withdrawUser
                        )
                    )

                    user.apply {
                        this.oauthInfo = oauthInfo.withdrawOAuthInfo()
                    }.run { userService.saveSync(this) }

                    postService.saveAllSync(deactivatedPosts)

                    userStatus.apply {
                        accountStatusId = userStatusTypeService.getDeletedStatusId()
                    }.run { userStatusService.saveSync(this) }
                }
            }

            val oAuthDeferred = async {
                oAuthService.withdraw(user.oauthInfo)
            }

            awaitAll(txDeferred, oAuthDeferred)
        }
    }
}
