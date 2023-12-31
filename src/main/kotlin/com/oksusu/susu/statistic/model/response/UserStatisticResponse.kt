package com.oksusu.susu.statistic.model.response

import com.oksusu.susu.statistic.domain.SusuBasicStatistic
import com.oksusu.susu.statistic.domain.UserStatistic
import com.oksusu.susu.statistic.model.TitleValueModel

class UserStatisticResponse(
    /** 최근 사용 금액 */
    val recentSpent: List<TitleValueModel>?,
    /** 경조사비를 가장 많이 쓴 달 */
    val mostSpentMonth: Long?,
    /** 최다 수수 관계 */
    val mostRelationship: TitleValueModel?,
    /** 최다 수수 경조사 */
    val mostCategory: TitleValueModel?,
    /** 가장 많이 받은 금액 */
    val highestAmountReceived: TitleValueModel?,
    /** 가장 많이 보낸 금액 */
    val highestAmountSent: TitleValueModel?,
) {
    companion object {
        fun of(
            basicStatistic: SusuBasicStatistic,
            receivedMaxAmountModel: TitleValueModel?,
            sentMaxAmountModel: TitleValueModel?,
        ): UserStatisticResponse {
            return UserStatisticResponse(
                recentSpent = basicStatistic.recentSpent,
                mostSpentMonth = basicStatistic.mostSpentMonth,
                mostRelationship = basicStatistic.relationship,
                mostCategory = basicStatistic.category,
                highestAmountReceived = receivedMaxAmountModel,
                highestAmountSent = sentMaxAmountModel
            )
        }

        fun from(userStatistic: UserStatistic): UserStatisticResponse {
            return UserStatisticResponse(
                recentSpent = userStatistic.recentSpent,
                mostSpentMonth = userStatistic.mostSpentMonth,
                mostRelationship = userStatistic.mostRelationship,
                mostCategory = userStatistic.mostCategory,
                highestAmountReceived = userStatistic.highestAmountReceived,
                highestAmountSent = userStatistic.highestAmountSent
            )
        }
    }
}
