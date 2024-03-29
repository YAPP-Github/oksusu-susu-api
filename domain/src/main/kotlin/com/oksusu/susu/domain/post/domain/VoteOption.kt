package com.oksusu.susu.domain.post.domain

import com.oksusu.susu.domain.common.BaseEntity
import jakarta.persistence.*

/** 투표 옵션 */
@Entity
@Table(name = "vote_option")
class VoteOption(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    /** 연관된 투표 id */
    @Column(name = "post_id")
    val postId: Long,

    /** 내용 */
    val content: String,

    /** 순서 */
    val seq: Int,
) : BaseEntity()
