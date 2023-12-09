package com.kotlin.orders.service

import com.kotlin.orders.dto.OrderDTO
import com.kotlin.orders.entity.Order
import com.kotlin.orders.mapper.OrderMapperImpl
import com.kotlin.orders.repository.CustomerRepository
import com.kotlin.orders.repository.ItemRepository
import com.kotlin.orders.repository.OrderRepository
import com.kotlin.orders.repository.TruckRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class OrderService(
    private val customerRepository : CustomerRepository,
    private val truckRepository : TruckRepository,
    private val itemRepository : ItemRepository,
    private val orderRepository : OrderRepository,
    private val orderMapperImpl : OrderMapperImpl

) {

    fun createOrder(orderDTO: OrderDTO): Order {
        val customer = customerRepository.findById(orderDTO.customer.id!!)
            .orElseThrow { EntityNotFoundException("Customer not found with ID ${orderDTO.customer.id}") }

        val truck = truckRepository.findById(orderDTO.truckId)
            .orElseThrow { EntityNotFoundException("Truck not found with ID ${orderDTO.truckId}") }

        val orderEntity = Order(
            id = null,
            customer = customer,
            items = orderDTO.items.map { itemRepository.findById(it).get() },
            truck = truck,
            date = LocalDate.now(),
            totalPrice = orderDTO.items.map { itemRepository.findById(it).get().price }.sum()
        )

        return orderRepository.save(orderEntity)
    }

    fun getOrders(pageable: Pageable): Page<OrderDTO> {
        return orderRepository.findAll(pageable).map { orderMapperImpl.toDto(it) }
    }

    fun getOrdersByCustomer(phoneNumber: String): List<Order> {
        return orderRepository.findByCustomerPhoneNumber(phoneNumber)
    }

    fun getOrdersByCustomerPaged(pageable: Pageable, phoneNumber: String): Page<OrderDTO> {
        return orderRepository.findByCustomerPhoneNumber(phoneNumber, pageable).map { orderMapperImpl.toDto(it) }
    }

    fun getOrdersByTruckIdAndDate(truckId: Int, deliveryDate: LocalDate, pageable: Pageable): Page<Order> {
        val endOfDay = deliveryDate.plusDays(1)
        return orderRepository.findByTruckIdAndDate(truckId, deliveryDate, endOfDay, pageable)
    }

    fun getTotalPriceByTruckIdAndDate(truckId: Int, deliveryDate: LocalDate, pageable: Pageable): Double {
        return getOrdersByTruckIdAndDate(truckId, deliveryDate, pageable).content.sumOf { it.totalPrice }
    }
}