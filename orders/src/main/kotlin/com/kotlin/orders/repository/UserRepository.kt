package com.kotlin.orders.repository

import com.kotlin.orders.entity.User
import com.kotlin.orders.entity.Role
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class UserRepository {

    private val users = mutableListOf(
            User(
                    id = UUID.randomUUID(),
                    email = "user1@example.com",
                    password = "password1",
                    role = Role.USER.toString()
            ),
            User(
                    id = UUID.randomUUID(),
                    email = "user2@example.com",
                    password = "password2",
                    role = Role.USER.toString()
            ),
            User(
                    id = UUID.randomUUID(),
                    email = "admin@example.com",
                    password = "adminpassword",
                    role = Role.ADMIN.toString()
            )
    )

    fun save(user: User): Boolean =
        users.add(user)

    fun findByEmail(email: String): User? =
            users.firstOrNull { it.email == email }

    fun findByUUID(id: UUID): User? =
            users.firstOrNull { it.id == id }

    fun findAll(): List<User> =
            users

    fun deleteById(id: UUID): Boolean =
            users.removeIf { it.id == id }


}