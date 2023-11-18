package com.kotlin.orders.service

import com.kotlin.orders.dto.OrderDTO
import com.kotlin.orders.entity.Order
import com.kotlin.orders.repository.CustomerRepository
import com.kotlin.orders.repository.ItemRepository
import com.kotlin.orders.repository.OrderRepository
import com.kotlin.orders.repository.TruckRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class OrderService(val customerRepository : CustomerRepository,
                                   val truckRepository : TruckRepository,
                                   val itemRepository : ItemRepository,
                                   val orderRepository : OrderRepository
) {

    fun createOrder(orderDTO: OrderDTO): Order {
        val customer = orderDTO.customerId.let {
            customerRepository.findById(it)
                .orElseThrow { EntityNotFoundException("Customer not found with ID ${orderDTO.customerId}") }
        }

        val truck = orderDTO.truckId.let {
            truckRepository.findById(it)
                .orElseThrow { EntityNotFoundException("Truck not found with ID ${orderDTO.truckId}") }
        }

        val items = orderDTO.items.map {
            itemRepository.findById(it).get().id?.let { it1 -> itemRepository.findById(it1).get() }
                ?: throw EntityNotFoundException("Item not found with ID $it")
        }

        val orderEntity = Order(
            id = null,
            customer = customer!!,
            items = items,
            truck = truck!!,
            date = LocalDate.now(),
            totalPrice = items.sumOf { it.price * it.quantity }
        )

        orderRepository.save(orderEntity)

        return orderEntity
    }

    fun getOrders(): Page<Order> {
        val orders = orderRepository.findAll()

        return PageImpl(orders.map { order ->
            Order(
                id = order.id,
                customer = order.customer,
                items = order.items,
                truck = order.truck,
                date = order.date,
                totalPrice = order.totalPrice
            )
        })
    }

    fun getOrdersByCustomer(phoneNumber: String): List<Order> {
        val orders = orderRepository.findAll()
    
        return orders.filter { it.customer.phoneNumber == phoneNumber }.map { order ->
            Order(
                id = order.id,
                customer = order.customer,
                items = order.items,
                truck = order.truck,
                date = order.date,
                totalPrice = order.totalPrice
            )
        }
    }

    fun getOrdersByCustomerPaged(pageable: Pageable, phoneNumber: String): Page<Order> {
        val orders = orderRepository.findByCustomerPhoneNumber(phoneNumber, pageable)
        return orders.map { order ->
            order?.let {
                Order(
                    id = order.id,
                    customer = it.customer,
                    items = order.items,
                    truck = order.truck,
                    date = order.date,
                    totalPrice = order.totalPrice
                )
            }
        }
    }

    fun getOrdersByTruckIdAndDate(truckId: Int, deliveryDate: LocalDate, pageable: Pageable): Page<Order> {
        val endOfDay = deliveryDate.plusDays(1)
        return orderRepository.findByTruckIdAndDate(truckId, deliveryDate, endOfDay, pageable)
    }

    fun getTotalPriceByTruckIdAndDate(truckId: Int, deliveryDate: LocalDate, pageable: Pageable): Double {
        val orders = getOrdersByTruckIdAndDate(truckId, deliveryDate, pageable)
        return orders.content.sumOf { it.totalPrice }
    }
}

