package com.group.libraryapp.domain.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository : JpaRepository<User, Long>, UserRepositoryCustom {

    fun findByName(name: String) : User?

    // JPQL 사용 케이스
    // Querydsl 로 리팩토링 완료
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.userLoanHistory")
    fun findAllWithHistory(): List<User>
}