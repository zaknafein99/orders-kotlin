package com.kotlin.orders.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    val customer: Customer,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "order_items",
        joinColumns = [JoinColumn(name = "order_id")],
        inverseJoinColumns = [JoinColumn(name = "item_id")]
    )
    val items: List<Item>,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "truck_id")
    val truck: Truck,

    val date: LocalDateTime,

    val totalPrice: Double
)