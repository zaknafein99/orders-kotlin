package com.kotlin.orders.repository

import com.kotlin.orders.user.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<User, Int> {

    fun findByName(name: String): Optional<User>
}