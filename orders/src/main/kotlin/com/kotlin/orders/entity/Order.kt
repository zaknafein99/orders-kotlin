package com.kotlin.orders.entity

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "orders")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator::class, property = "id")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Int? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    val customer: Customer,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "order_items",
        joinColumns = [JoinColumn(name = "order_id")],
        inverseJoinColumns = [JoinColumn(name = "item_id")]
    )
    val items: List<Item>,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "truck_id")
    val truck: Truck,

    val date: LocalDate,

    val totalPrice: Double
)