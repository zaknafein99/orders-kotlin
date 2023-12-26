package com.kotlin.orders.repository

import com.kotlin.orders.entity.User
import com.kotlin.orders.entity.Role
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class UserRepository(private val encoder: PasswordEncoder) {

    private val users = mutableListOf(
            User(
                    id = UUID.randomUUID(),
                    email = "user1@example.com",
                    password = encoder.encode("password1"),
                    role = Role.USER
            ),
            User(
                    id = UUID.randomUUID(),
                    email = "user2@example.com",
                    password = encoder.encode("password2"),
                    role = Role.USER
            ),
            User(
                    id = UUID.randomUUID(),
                    email = "admin@example.com",
                    password = encoder.encode("adminpassword"),
                    role = Role.ADMIN
            )
    )

    fun save(user: User): Boolean {
        val updated = user.copy(password = encoder.encode(user.password))
        return users.add(updated)
    }
    fun findByEmail(email: String): User? =
            users.firstOrNull { it.email == email }

    fun findByUUID(id: UUID): User? =
            users.firstOrNull { it.id == id }

    fun findAll(): List<User> =
            users

    fun deleteById(id: UUID): Boolean =
            users.removeIf { it.id == id }


}