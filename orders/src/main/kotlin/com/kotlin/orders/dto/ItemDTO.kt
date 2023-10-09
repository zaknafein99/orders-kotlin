package com.kotlin.orders.dto

data class ItemDTO(
    val id: Int?,
    val name: String,
    val description: String,
    val price: Double,
    val quantity: Int,
    val category: String)