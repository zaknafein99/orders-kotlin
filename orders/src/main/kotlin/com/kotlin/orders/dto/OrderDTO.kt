package com.kotlin.orders.dto

import jakarta.annotation.Nullable
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class OrderDTO(
    val id: Int?,
    @get:NotBlank(message = "El cliente no puede estar vacío")
    val customer: CustomerDTO,
    @get:NotBlank(message = "Debe haber al menos un producto")
    val items: List<ItemDTO>,
    val truck: TruckDTO,
    val date: LocalDateTime,
    @Nullable
    val totalPrice: Double
)