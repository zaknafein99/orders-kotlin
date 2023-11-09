package com.kotlin.orders.repository

import com.kotlin.orders.entity.Order
import org.springframework.data.domain.Page
import org.springframework.data.repository.CrudRepository
import org.springframework.data.domain.Pageable

interface OrderRepository : CrudRepository<Order, Int> {

    fun findByCustomerPhoneNumber(phoneNumber: String, pageable: Pageable): Page<Order?>

}
