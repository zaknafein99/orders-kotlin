package com.kotlin.orders.controller

import com.kotlin.orders.dto.OrderDTO
import com.kotlin.orders.entity.Order
import com.kotlin.orders.service.OrderService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/orders")
class OrderController(val orderService: OrderService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createOrder(@RequestBody orderDTO : OrderDTO): ResponseEntity<Order> {

        return ResponseEntity.ok(orderService.createOrder(orderDTO))
        }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getOrders(@PageableDefault(page = 0, size = 10) pageable: Pageable): Page<Order> = orderService.getOrdersPaged()

    @GetMapping("/customer")
    @ResponseStatus(HttpStatus.OK)
    fun getOrdersByCustomer(@PageableDefault(page = 0, size = 10) pageable: Pageable, @RequestParam phoneNumber: String): Page<Order> = orderService.getOrdersByCustomerPaged(pageable, phoneNumber )

    @GetMapping("/{customer_phone_number}")
    @ResponseStatus(HttpStatus.OK)
    fun getOrdersByCustomerPaged(@PathVariable("customer_phone_number") customerPhoneNumber: String): List<Order> = orderService.getOrdersByCustomer(customerPhoneNumber)
    }
