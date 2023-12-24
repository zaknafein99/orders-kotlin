package com.kotlin.orders.controller.user

import com.kotlin.orders.entity.User
import com.kotlin.orders.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*

@RestController
@RequestMapping("/user")
class UserController(private val userService: UserService) {

    @PostMapping
    fun create(@RequestBody userRequest: UserRequest): UserResponse =
            userService.create(
                    user = userRequest.toModel()
            )?.toResponse()
             ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User already exists")

    @GetMapping
    fun listAll(): List<UserResponse> =
            userService.findAll()
                    .map { it.toResponse() }

    @GetMapping("/{uuid}")
    fun findByUuid(@PathVariable uuid: UUID): UserResponse =
            userService.findByUUID(uuid)
                    ?.toResponse()
             ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

    @DeleteMapping("/{uuid}")
    fun deleteByUuid(@PathVariable uuid: UUID) : ResponseEntity<Boolean> {
        val success = userService.deleteById(uuid)

        return if (success) {
            ResponseEntity.noContent().build()
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        }
    }

    private fun UserRequest.toModel() : User =
            User(
                    id = UUID.randomUUID(),
                    email = this.email,
                    password = this.password,
                    role = "USER",
                    )

    private fun User.toResponse() : UserResponse =
            UserResponse(
                    id = this.id,
                    email = this.email,
            )
}