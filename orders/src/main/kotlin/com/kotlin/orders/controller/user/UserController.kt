package com.kotlin.orders.controller.user

import com.kotlin.orders.entity.User
import com.kotlin.orders.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
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