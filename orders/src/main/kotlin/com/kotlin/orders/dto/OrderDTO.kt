package com.kotlin.orders.dto

import jakarta.annotation.Nullable
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class OrderDTO(
    val id: Int?,
    @get:NotBlank(message = "El cliente no puede estar vacío")
    val customerId: Int,
    @get:NotBlank(message = "Debe haber al menos un producto")
    val items: List<Int>,
    val truckId: Int,
//    val date: LocalDateTime,
    @Nullable
    val totalPrice: Double
)