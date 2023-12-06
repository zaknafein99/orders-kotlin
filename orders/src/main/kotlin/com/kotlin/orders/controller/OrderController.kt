package com.kotlin.orders.controller

import com.kotlin.orders.dto.OrderDTO
import com.kotlin.orders.entity.Order
import com.kotlin.orders.service.OrderService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/orders")
class OrderController(val orderService: OrderService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createOrder(@RequestBody orderDTO: OrderDTO): ResponseEntity<Order> =
        ResponseEntity.ok(orderService.createOrder(orderDTO))

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getOrders(@PageableDefault(page = 0, size = 10) pageable: Pageable): Page<OrderDTO> =
        orderService.getOrders(pageable)

    @GetMapping("/customer")
    @ResponseStatus(HttpStatus.OK)
    fun getOrdersByCustomer(@PageableDefault(page = 0, size = 10) pageable: Pageable,
                            @RequestParam phoneNumber: String): Page<OrderDTO> =
        orderService.getOrdersByCustomerPaged(pageable, phoneNumber)

    @GetMapping("/{customer_phone_number}")
    @ResponseStatus(HttpStatus.OK)
    fun getOrdersByCustomerPaged(@PathVariable("customer_phone_number") customerPhoneNumber: String): List<Order> =
        orderService.getOrdersByCustomer(customerPhoneNumber)

    @GetMapping("/truck/{truckId}")
    fun getOrdersByTruckIdAndDate(@PageableDefault(page = 0, size = 10) pageable: Pageable,
                                  @PathVariable truckId: Int,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) deliveryDate: LocalDate): ResponseEntity<Map<String, Any>> {
        val orders = orderService.getOrdersByTruckIdAndDate(truckId, deliveryDate, pageable)
        val dayTotalPrice = orderService.getTotalPriceByTruckIdAndDate(truckId, deliveryDate, pageable)
        val response = mapOf("orders" to orders, "dayTotalPrice" to dayTotalPrice)
        return ResponseEntity.ok().body(response)
    }
}
