package com.kotlin.orders.entity

import jakarta.persistence.*

@Entity
@Table(name = "customers")
data class Customer (
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Int?,
    val name: String,
    val address: String,
    val phoneNumber: String)
