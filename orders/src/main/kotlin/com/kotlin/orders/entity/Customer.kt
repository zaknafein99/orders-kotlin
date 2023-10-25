package com.kotlin.orders.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "customers")
data class Customer (
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val uuid: UUID?,
    var name: String,
    var address: String,
    var phoneNumber: String,
    var type: String,
    var state: String,
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    val orders: MutableList<Order> = mutableListOf()
)
