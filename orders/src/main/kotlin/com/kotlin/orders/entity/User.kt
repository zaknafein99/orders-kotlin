package com.kotlin.orders.entity

import java.util.UUID

data class User(
        val id: UUID,
        val email: String,
        val password: String,
        val role: String
)

enum class Role {
    USER,
    ADMIN
}
