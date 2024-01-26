package com.oksusu.susu.post.model.vo

data class SearchVoteRequest(
    val content: String?,
    val mine: Boolean?,
    val sortType: VoteSortType?,
    val postCategoryId: Long?,
    val size: Int?,
)

enum class VoteSortType {
    /** 최신 작성 순 */
    LATEST,

    /** 투표 많은 순 */
    POPULAR,
    ;
}
