package com.oksusu.susu.post.application

import com.oksusu.susu.common.util.toTypeReference
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NotFoundException
import com.oksusu.susu.extension.decodeBase64
import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.domain.vo.PostType
import com.oksusu.susu.post.infrastructure.repository.PostRepository
import com.oksusu.susu.post.infrastructure.repository.model.AllVoteCursorModel
import com.oksusu.susu.post.infrastructure.repository.model.GetAllVoteSpec
import com.oksusu.susu.post.infrastructure.repository.model.PopularVoteCursorModel
import com.oksusu.susu.post.infrastructure.repository.model.PostAndVoteOptionModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.domain.KeysetScrollPosition
import org.springframework.data.domain.ScrollPosition
import org.springframework.data.domain.Window
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class VoteService(
    private val postService: PostService,
    private val postRepository: PostRepository,
) {
    val logger = mu.KotlinLogging.logger { }

    suspend fun getAllVotesExceptBlock(spec: GetAllVoteSpec, cursor: String?): Pair<Window<Post>, String?> {
        val keysetScrollPosition = cursor?.decodeBase64(toTypeReference<AllVoteCursorModel>())
            ?.toKeysetScrollPosition(ScrollPosition.Direction.FORWARD)
            ?: getDefaultAllVoteScrollPosition()

        logger.info { keysetScrollPosition }

        spec.apply { this.keysetScrollPosition = keysetScrollPosition }

        val votes = withContext(Dispatchers.IO) {
            postRepository.getAllVotesExceptBlock(spec)
        }

        val nextCursor = votes.content.size.takeIf { size -> size > 0 }?.let { size ->
            AllVoteCursorModel.toBase64Model(votes.positionAt(size - 1) as KeysetScrollPosition)
        }

        return votes to nextCursor
    }

    fun getDefaultAllVoteScrollPosition(): KeysetScrollPosition {
        val keys = mutableMapOf<String, Any>()
        keys["id"] = 0L
        keys["createdAt"] = LocalDateTime.now()

        return ScrollPosition.forward(keys)
    }

    suspend fun getAllVotesOrderByPopular(spec: GetAllVoteSpec, cursor: String?): Pair<Window<Post>, String?> {
        val keysetScrollPosition = cursor?.decodeBase64(toTypeReference<PopularVoteCursorModel>())
            ?.toKeysetScrollPosition(ScrollPosition.Direction.FORWARD)
            ?: getDefaultPopularVoteScrollPosition()

        logger.info { keysetScrollPosition }

        spec.apply { this.keysetScrollPosition = keysetScrollPosition }

        val votes = withContext(Dispatchers.IO) {
            postRepository.getPopularVotesExceptBlock(spec)
        }.map { model -> model.post }

        val nextCursor = votes.content.size.takeIf { size -> size > 0 }?.let { size ->
            PopularVoteCursorModel.toBase64Model(votes.positionAt(size - 1) as KeysetScrollPosition)
        }

        return votes to nextCursor
    }

    fun getDefaultPopularVoteScrollPosition(): KeysetScrollPosition {
        val keys = mutableMapOf<String, Any>()
        keys["id"] = 0L
        keys["createdAt"] = LocalDateTime.now()
        keys["count"] = Long.MAX_VALUE

        return ScrollPosition.forward(keys)
    }

    suspend fun getVote(id: Long): Post {
        return postService.findByIdAndIsActiveAndTypeOrThrow(id, true, PostType.VOTE)
    }

    suspend fun getVoteAndOptions(id: Long): List<PostAndVoteOptionModel> {
        return withContext(Dispatchers.IO) {
            postRepository.getVoteAndOptions(id)
        }.takeUnless { it.isEmpty() } ?: throw NotFoundException(ErrorCode.NOT_FOUND_VOTE_ERROR)
    }

//    suspend fun getPopularVotesExceptBlock(
//        uid: Long,
//        userBlockIds: List<Long>,
//        postBlockIds: List<Long>,
//        size: Int,
//    ): List<PostAndCountModel> {
//        val getAllVoteSpec = GetAllVoteSpec(
//            uid = uid,
//            searchSpec = SearchVoteSpec.defaultPopularSpec(),
//            userBlockIds = userBlockIds,
//            postBlockIds = postBlockIds,
//            pageable = PageRequest.of(0, size)
//        )
//
//        return withContext(Dispatchers.IO) { postRepository.getPopularVotesExceptBlock(getAllVoteSpec) }.content
//    }

}
