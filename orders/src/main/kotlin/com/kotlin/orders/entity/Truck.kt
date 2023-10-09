package com.kotlin.orders.entity

import jakarta.persistence.*

@Entity
@Table(name = "trucks")
data class Truck(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Int?,
    val name: String,
)