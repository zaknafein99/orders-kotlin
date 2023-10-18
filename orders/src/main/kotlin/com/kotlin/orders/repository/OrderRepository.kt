package com.kotlin.orders.repository

import com.kotlin.orders.entity.Order
import org.springframework.data.repository.CrudRepository

interface OrderRepository : CrudRepository<Order, Int> {

}
