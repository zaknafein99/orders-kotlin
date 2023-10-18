package com.kotlin.orders.controller

import com.kotlin.orders.dto.OrderDTO
import com.kotlin.orders.service.OrderService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/orders")
class OrderController(val orderService: OrderService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createOrder(@RequestBody orderDTO : OrderDTO): ResponseEntity<OrderDTO> {

        return ResponseEntity.ok(orderService.createOrder(orderDTO))
        }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getOrders(): List<OrderDTO> = orderService.getOrders()

    }
