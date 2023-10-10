package com.kotlin.orders.entity

import jakarta.persistence.*

@Entity
@Table(name = "items")
data class Item(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Int?,
    var name: String,
    var description: String,
    var price: Double,
    var quantity: Int,
    var category: String)