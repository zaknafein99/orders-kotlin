package com.kotlin.orders.dto

import com.kotlin.orders.entity.Item
import com.kotlin.orders.entity.Truck
import jakarta.annotation.Nullable
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.NotBlank

data class OrderDTO(
    val id: Int?,
    @get:NotNull(message = "El cliente no puede estar vacío")
    val customer: CustomerDTO,
    @get:NotNull(message = "Debe haber al menos un producto")
    val items: List<Item>,
    val truck: Truck,
    val date: LocalDate?,
    @get:NotBlank(message = "Debe haber al menos un producto")
    val items: List<Int>,
    val truckId: Int,
    val date: LocalDate,
    @Nullable
    val totalPrice: Double
)