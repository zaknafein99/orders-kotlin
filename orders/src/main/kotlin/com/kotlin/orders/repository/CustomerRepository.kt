package com.kotlin.orders.repository

import com.kotlin.orders.entity.Customer
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*


interface CustomerRepository : JpaRepository<Customer, Int> {

    fun findByPhoneNumber(phoneNumber: String): List<Customer?>
    abstract fun findByUuid(it: UUID): Optional<Customer>


}