package com.kotlin.orders.dto

data class CustomerDTO(
    val id: Int?,
    val name: String,
    val address: String,
    val phoneNumber: String)