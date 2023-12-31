package com.oksusu.susu.auth.model.dto

import com.oksusu.susu.auth.model.OauthProvider
import com.oksusu.susu.client.oauth.kakao.model.KakaoOauthUserInfoResponse
import com.oksusu.susu.user.domain.OauthInfo

class OauthUserInfoDto(
    val oauthInfo: OauthInfo,
) {
    companion object {
        fun fromKakao(kakaoOauthUserInfoResponse: KakaoOauthUserInfoResponse): OauthUserInfoDto {
            return OauthUserInfoDto(
                OauthInfo(
                    oauthId = kakaoOauthUserInfoResponse.id,
                    oauthProvider = OauthProvider.KAKAO
                )
            )
        }
    }
}
