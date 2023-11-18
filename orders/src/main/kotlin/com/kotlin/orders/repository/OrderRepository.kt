package com.kotlin.orders.repository

import com.kotlin.orders.entity.Order
import org.springframework.data.domain.Page
import org.springframework.data.repository.CrudRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface OrderRepository : CrudRepository<Order, Int> {

    fun findByCustomerPhoneNumber(phoneNumber: String, pageable: Pageable): Page<Order?>

    @Query("SELECT o FROM Order o WHERE o.truck.id = :truckId AND o.date >= :startOfDay AND o.date < :endOfDay")
    fun findByTruckIdAndDate(@Param("truckId") truckId: Int,
                             @Param("startOfDay") startOfDay: LocalDate,
                             @Param("endOfDay") endOfDay: LocalDate, pageable: Pageable): Page<Order>
}