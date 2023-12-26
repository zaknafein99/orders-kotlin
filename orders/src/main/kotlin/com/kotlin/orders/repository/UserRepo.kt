package com.kotlin.orders.repository

import com.kotlin.orders.entity.User
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepo : CrudRepository<User, Int> {
    abstract fun findByEmail(email: String): Any
    fun findById(id: UUID): Any
    abstract fun deleteById(id: UUID): Any
}