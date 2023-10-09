package com.kotlin.orders.controller

import com.kotlin.orders.dto.CustomerDTO
import com.kotlin.orders.service.CustomerService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/customer")
class CustomerController(val customerService: CustomerService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addCustomer(@RequestBody customerDTO: CustomerDTO): CustomerDTO {
        return customerService.addCustomer(customerDTO)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getCustomers(): List<CustomerDTO> {
        return customerService.getCustomers()
    }

    @PostMapping("/list")
    @ResponseStatus(HttpStatus.CREATED)
    fun addListOfCustomers (@RequestBody customerDTO: List<CustomerDTO>): List<CustomerDTO> {
        return customerService.addListOfCustomers(customerDTO)
    }
}