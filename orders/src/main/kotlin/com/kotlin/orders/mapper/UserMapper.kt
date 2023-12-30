package com.kotlin.orders.mapper

import com.kotlin.orders.controller.user.UserRequest
import com.kotlin.orders.controller.user.UserResponse
import com.kotlin.orders.entity.User
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")

interface UserMapper {

    fun userToUserResponse(user: User): UserResponse

    fun userResponseToUser(userResponse: UserResponse): User

    fun userToUserRequest(user: User): UserRequest

    fun userRequestToUser(userRequest: UserRequest): User

    fun userRequestToUserResponse(userRequest: UserRequest): UserResponse

    fun userResponseToUserRequest(userResponse: UserResponse): UserRequest

}