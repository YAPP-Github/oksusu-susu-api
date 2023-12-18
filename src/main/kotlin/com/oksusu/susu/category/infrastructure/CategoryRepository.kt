package com.oksusu.susu.category.infrastructure

import com.oksusu.susu.category.domain.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<Category, Long>
