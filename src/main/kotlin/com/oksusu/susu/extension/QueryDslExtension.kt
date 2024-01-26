package com.oksusu.susu.extension

import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.SusuException
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.NumberPath
import com.querydsl.core.types.dsl.StringPath
import com.querydsl.jpa.impl.JPAQuery
import org.springframework.data.domain.*
import org.springframework.data.jpa.repository.support.Querydsl

fun <T> Querydsl?.execute(query: JPAQuery<T>, pageable: Pageable): Page<T> {
    return this.takeUnless { querydsl -> querydsl == null }
        ?.let { queryDsl ->
            queryDsl.applyPagination(pageable, query).run {
                PageImpl(this.fetch(), pageable, this.fetchCount())
            }
        } ?: throw SusuException(ErrorCode.QUERY_DSL_NOT_EXISTS_ERROR)
}

fun <T> Querydsl?.executeSlice(query: JPAQuery<T>, pageable: Pageable): Slice<T> {
    return this.takeUnless { querydsl -> querydsl == null }
        ?.let { queryDsl ->
            queryDsl.applyPagination(pageable, query).run {
                this.limit(pageable.pageSize + 1L)
                    .fetch()
            }.run {
                var hasNext = false
                if (this.size > pageable.pageSize) {
                    hasNext = true
                    this.removeAt(pageable.pageSize)
                }
                SliceImpl(this, pageable, hasNext)
            }
        } ?: throw SusuException(ErrorCode.QUERY_DSL_NOT_EXISTS_ERROR)
}

fun <T> JPAQuery<T>.executeWindow(size: Int, keysetScrollPosition: KeysetScrollPosition): Window<T> {
    return this.limit(size + 1L).fetch()
        .run {
            var hasNext = false
            if (this.size > size) {
                hasNext = true
                this.removeAt(size)
            }
            Window.from(
                this,
                { idx ->
                    val idxObject = this[idx]
                    val propertyValue = idxObject!!.getPropertyValues()
                    val curKeys = keysetScrollPosition.keys
                    val idxKeys = curKeys.map { curKey -> curKey.key to propertyValue[curKey.key] }.toMap()

                    when (keysetScrollPosition.direction) {
                        ScrollPosition.Direction.FORWARD -> ScrollPosition.forward(idxKeys)
                        ScrollPosition.Direction.BACKWARD -> ScrollPosition.backward(idxKeys)
                    }
                },
                hasNext
            )
        }
}

fun StringPath.isEquals(parameter: String?): BooleanExpression? {
    return parameter?.let { this.eq(parameter) }
}

fun NumberPath<Long>.isEquals(parameter: Long?): BooleanExpression? {
    return parameter?.let { this.eq(parameter) }
}

fun StringPath.isContains(parameter: String?): BooleanExpression? {
    return parameter?.let { this.contains(parameter) }
}

fun NumberPath<Long>.isIn(parameters: Set<Long>?): BooleanExpression? {
    return parameters.takeUnless { params -> params.isNullOrEmpty() }?.let { params -> this.`in`(params) }
}

fun NumberPath<Long>.isNotIn(parameters: Set<Long>?): BooleanExpression? {
    return parameters.takeUnless { params -> params.isNullOrEmpty() }?.let { params -> this.notIn(params) }
}
