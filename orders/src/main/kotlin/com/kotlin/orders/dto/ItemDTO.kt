package com.kotlin.orders.dto

import jakarta.validation.constraints.NotBlank

data class ItemDTO(
    val id: Int?,
    @get:NotBlank(message = "El nombre no puede estar vacío")
    val name: String,
    val description: String,
    @get:NotBlank(message = "El precio no puede estar vacío")
    val price: Double,
    val quantity: Int,
    val category: String)