package com.kotlin.orders.repository

import com.kotlin.orders.entity.Customer
import org.springframework.data.repository.CrudRepository

interface CustomerRepository : CrudRepository<Customer, Int>{

    fun findByPhoneNumber(phoneNumber: String): List<Customer?>

}