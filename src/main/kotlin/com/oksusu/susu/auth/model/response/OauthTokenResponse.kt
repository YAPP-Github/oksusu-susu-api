package com.oksusu.susu.auth.model.response

import com.oksusu.susu.client.oauth.kakao.model.KakaoOauthTokenResponse

class OauthTokenResponse(
    val accessToken: String,
    val refreshToken: String,
) {
    companion object {
        fun fromKakao(kakaoOauthTokenResponse: KakaoOauthTokenResponse): OauthTokenResponse {
            return OauthTokenResponse(
                accessToken = kakaoOauthTokenResponse.accessToken,
                refreshToken = kakaoOauthTokenResponse.refreshToken
            )
        }
    }
}