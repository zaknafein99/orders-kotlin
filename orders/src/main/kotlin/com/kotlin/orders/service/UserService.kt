package com.kotlin.orders.service

import com.kotlin.orders.entity.User
import com.kotlin.orders.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
        private val userRepository: UserRepository
) {

    fun findByEmail(email: String) =
            userRepository.findByEmail(email)

    fun findByUUID(id: UUID) =
            userRepository.findByUUID(id)

    fun findAll() =
            userRepository.findAll()

    fun deleteById(id: UUID) =
            userRepository.deleteById(id)

    fun create(user: User): User? {
        val existingUser = userRepository.findByEmail(user.email)
        return if (existingUser == null) {
            userRepository.save(user)
            user
        } else {
            null
        }
    }
}