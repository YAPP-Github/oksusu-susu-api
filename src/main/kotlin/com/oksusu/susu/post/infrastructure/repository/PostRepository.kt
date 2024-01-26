package com.oksusu.susu.post.infrastructure.repository

import com.oksusu.susu.count.domain.QCount
import com.oksusu.susu.extension.*
import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.domain.QPost
import com.oksusu.susu.post.domain.QPostCategory
import com.oksusu.susu.post.domain.QVoteOption
import com.oksusu.susu.post.domain.vo.PostType
import com.oksusu.susu.post.infrastructure.repository.model.*
import com.querydsl.jpa.impl.JPAQuery
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.KeysetScrollPosition
import org.springframework.data.domain.ScrollPosition
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Window
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
interface PostRepository : JpaRepository<Post, Long>, PostCustomRepository {
    @Transactional(readOnly = true)
    fun findByIdAndIsActiveAndType(id: Long, isActive: Boolean, type: PostType): Post?

    @Transactional(readOnly = true)
    fun findAllBy(position: KeysetScrollPosition): Window<Post>
//    fun findFirst3OrderByCreatedAt(position: ScrollPosition): Window<Post>

    fun findFirst3ByIdNotIn(
        ids: List<Long>,
    ): List<Post>

    fun findFirst5ByUidNotInAndIdNotInAndIsActive(
        uids: Set<Long>,
        ids: Set<Long>,
        isActive: Boolean,
        sort: Sort,
        scrollPosition: ScrollPosition,
    ): Window<Post>
}

interface PostCustomRepository {
    @Transactional(readOnly = true)
    fun getVoteAndOptions(id: Long): List<PostAndVoteOptionModel>

    @Transactional(readOnly = true)
    fun getAllVotesExceptBlock(
        spec: GetAllVoteSpec,
    ): Window<Post>

    @Transactional(readOnly = true)
    fun getPopularVotesExceptBlock(
        spec: GetAllVoteSpec,
    ): Window<PostAndCountModel>
}

class PostCustomRepositoryImpl : PostCustomRepository, QuerydslRepositorySupport(Post::class.java) {
    @Autowired
    @Qualifier("susuEntityManager")
    override fun setEntityManager(entityManager: EntityManager) {
        super.setEntityManager(entityManager)
    }

    private val qPost = QPost.post
    private val qVoteOption = QVoteOption.voteOption
    private val qPostCategory = QPostCategory.postCategory
    private val qCount = QCount.count1

    override fun getVoteAndOptions(id: Long): List<PostAndVoteOptionModel> {
        return JPAQuery<QPost>(entityManager)
            .select(QPostAndVoteOptionModel(qPost, qVoteOption))
            .from(qPost)
            .leftJoin(qVoteOption).on(qPost.id.eq(qVoteOption.postId))
            .where(
                qPost.id.eq(id),
                qPost.isActive.eq(true),
                qPost.type.eq(PostType.VOTE)
            ).fetch()
    }

    override fun getAllVotesExceptBlock(spec: GetAllVoteSpec): Window<Post> {
        val uidFilter = spec.searchSpec.mine?.takeIf { mine -> mine }?.let {
            qPost.uid.isEquals(spec.uid)
        } ?: qPost.uid.isNotIn(spec.userBlockIds)
        val categoryFilter = qPost.postCategoryId.isEquals(spec.searchSpec.categoryId)
        val contentFilter = qPost.content.isContains(spec.searchSpec.content)
        val postIdFilter = qPost.id.isNotIn(spec.postBlockIds)

        val cursorCreatedAt = LocalDateTime.parse(spec.keysetScrollPosition.keys["createdAt"].toString())
        val cursorId = spec.keysetScrollPosition.keys["id"].toString().toLong()
        val cursorFilter = qPost.createdAt.lt(cursorCreatedAt)
            .or(
                qPost.createdAt.eq(cursorCreatedAt).and(
                    qPost.id.gt(cursorId)
                )
            )

        val query = JPAQuery<QPost>(entityManager)
            .select(qPost)
            .from(qPost)
            .where(
                qPost.isActive.eq(true),
                uidFilter,
                categoryFilter,
                postIdFilter,
                contentFilter,
                cursorFilter,
            ).orderBy(
                qPost.createdAt.desc()
            )

        return query.executeWindow(spec.searchSpec.size, spec.keysetScrollPosition)
    }

    override fun getPopularVotesExceptBlock(
        spec: GetAllVoteSpec,
    ): Window<PostAndCountModel> {
        val uidFilter = spec.searchSpec.mine?.takeIf { mine -> mine }?.let {
            qPost.uid.isEquals(spec.uid)
        } ?: qPost.uid.isNotIn(spec.userBlockIds)
        val categoryFilter = qPost.postCategoryId.isEquals(spec.searchSpec.categoryId)
        val contentFilter = qPost.content.isContains(spec.searchSpec.content)
        val postIdFilter = qPost.id.isNotIn(spec.postBlockIds)

        val cursorCount = spec.keysetScrollPosition.keys["count"].toString().toLong()
        val cursorCreatedAt = LocalDateTime.parse(spec.keysetScrollPosition.keys["createdAt"].toString())
        val cursorId = spec.keysetScrollPosition.keys["id"].toString().toLong()
        val cursorFilter = qCount.count.lt(cursorCount)
            .or(
                qCount.count.eq(cursorCount).and(
                    qPost.createdAt.lt(cursorCreatedAt)
                )
            )
            .or(
                qCount.count.eq(cursorCount).and(
                    qPost.createdAt.eq(cursorCreatedAt).and(
                        qPost.id.gt(cursorId)
                    )
                )
            )

        val query = JPAQuery<QPost>(entityManager)
            .select(
                QPostAndCountModel(
                    qPost,
                    qCount
                )
            )
            .from(qPost)
            .join(qCount).on(qPost.id.eq(qCount.targetId))
            .where(
                qPost.type.eq(PostType.VOTE),
                qPost.isActive.eq(true),
                uidFilter,
                categoryFilter,
                postIdFilter,
                contentFilter,
                cursorFilter,
            )
            .orderBy(qCount.count.desc())

        return query.executeWindow(spec.searchSpec.size, spec.keysetScrollPosition)
    }
}
