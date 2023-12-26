package com.kotlin.orders.service

import com.kotlin.orders.controller.user.UserResponse
import com.kotlin.orders.entity.User
import com.kotlin.orders.mapper.UserMapper
import com.kotlin.orders.repository.UserRepository
import mu.KLogging
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper,
    private val encoder: PasswordEncoder
) {

    companion object : KLogging()

    fun findByEmail(email: String) =
            userRepository.findByEmail(email)

    fun findByUUID(id: UUID) =
            userRepository.findById(id)

    fun findAll(): MutableIterable<User> =
            userRepository.findAll()

    fun deleteById(id: UUID) =
            userRepository.deleteById(id)

    fun create(user: User): UserResponse {
        user.password = encoder.encode(user.password)
        val savedUser = userRepository.save(user)
        return userMapper.userToUserResponse(savedUser)
    }
}