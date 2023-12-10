package com.kotlin.orders.dto

import com.kotlin.orders.entity.Item
import com.kotlin.orders.entity.Truck
import jakarta.annotation.Nullable
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

data class OrderDTO(
    val id: Int?,
    @get:NotBlank(message = "El cliente no puede estar vacío")
    val customer: CustomerDTO,
    @get:NotBlank(message = "Debe haber al menos un producto")
    val items: List<Item>,
    val truck: Truck,
    val date: LocalDate,
    @Nullable
    val totalPrice: Double
)