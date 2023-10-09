package com.kotlin.orders.entity

import jakarta.persistence.*

@Entity
@Table(name = "items")
data class Item(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Int?,
    val name: String,
    val description: String,
    val price: Double,
    val quantity: Int,
    val category: String)