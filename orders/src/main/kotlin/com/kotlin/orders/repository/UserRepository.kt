package com.kotlin.orders.repository

import com.kotlin.orders.entity.User
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : CrudRepository<User, Int> {
    fun findByEmail(email: String): Any
    fun findById(id: UUID): Any
    fun deleteById(id: UUID): Any
}