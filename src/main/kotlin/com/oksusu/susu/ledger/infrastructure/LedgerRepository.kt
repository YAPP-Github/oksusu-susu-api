package com.oksusu.susu.ledger.infrastructure

import com.oksusu.susu.category.domain.QCategoryAssignment
import com.oksusu.susu.category.domain.vo.CategoryAssignmentType
import com.oksusu.susu.extension.execute
import com.oksusu.susu.ledger.domain.Ledger
import com.oksusu.susu.ledger.domain.QLedger
import com.oksusu.susu.ledger.infrastructure.model.QSearchLedgerModel
import com.oksusu.susu.ledger.infrastructure.model.SearchLedgerModel
import com.oksusu.susu.ledger.infrastructure.model.SearchLedgerSpec
import com.querydsl.core.types.ExpressionUtils.allOf
import com.querydsl.jpa.impl.JPAQuery
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface LedgerRepository : JpaRepository<Ledger, Long>, LedgerCustomRepository

interface LedgerCustomRepository {
    @Transactional(readOnly = true)
    fun search(spec: SearchLedgerSpec, pageable: Pageable): Page<SearchLedgerModel>
}

class LedgerCustomRepositoryImpl : LedgerCustomRepository, QuerydslRepositorySupport(Ledger::class.java) {
    @Autowired
    @Qualifier("susuEntityManager")
    override fun setEntityManager(entityManager: EntityManager) {
        super.setEntityManager(entityManager)
    }

    private val qLedger = QLedger.ledger
    private val qCategoryAssignment = QCategoryAssignment.categoryAssignment

    override fun search(spec: SearchLedgerSpec, pageable: Pageable): Page<SearchLedgerModel> {
        val query = JPAQuery<QLedger>(entityManager)
            .select(QSearchLedgerModel(qLedger, qCategoryAssignment))
            .from(qLedger)
            .join(qCategoryAssignment).on(qLedger.id.eq(qCategoryAssignment.targetId))
            .where(
                qLedger.uid.eq(spec.uid),
                qCategoryAssignment.categoryId.eq(spec.categoryId),
                qCategoryAssignment.targetType.eq(CategoryAssignmentType.LEDGER),
                allOf(
                    qLedger.startAt.after(spec.fromStartAt),
                    qLedger.endAt.before(spec.toStartAt)
                )
            )

        return querydsl.execute(query, pageable)
    }
}
