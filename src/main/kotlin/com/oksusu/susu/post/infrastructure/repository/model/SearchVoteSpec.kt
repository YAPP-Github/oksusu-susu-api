package com.oksusu.susu.post.infrastructure.repository.model

import com.oksusu.susu.post.model.vo.SearchVoteRequest
import com.oksusu.susu.post.model.vo.VoteSortType

class SearchVoteSpec(
    val content: String?,
    val mine: Boolean?,
    val sortType: VoteSortType,
    val categoryId: Long?,
    val size: Int,
) {
    companion object {
        fun from(request: SearchVoteRequest): SearchVoteSpec {
            return SearchVoteSpec(
                content = request.content,
                mine = request.mine,
                sortType = request.sortType ?: VoteSortType.LATEST,
                categoryId = request.postCategoryId,
                size = request.size ?: 10
            )
        }

        fun defaultPopularSpec(): SearchVoteSpec {
            return SearchVoteSpec(
                content = null,
                mine = null,
                sortType = VoteSortType.POPULAR,
                categoryId = null,
                size = 10
            )
        }
    }
}
