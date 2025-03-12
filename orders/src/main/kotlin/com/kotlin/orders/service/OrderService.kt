package com.kotlin.orders.service

import com.kotlin.orders.dto.OrderDTO
import com.kotlin.orders.entity.Order
import com.kotlin.orders.entity.OrderStatus
import com.kotlin.orders.mapper.OrderMapper
import com.kotlin.orders.repository.CustomerRepository
import com.kotlin.orders.repository.ItemRepository
import com.kotlin.orders.repository.OrderRepository
import com.kotlin.orders.repository.TruckRepository
import jakarta.persistence.EntityNotFoundException
import java.time.LocalDate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class OrderService(
        private val customerRepository: CustomerRepository,
        private val truckRepository: TruckRepository,
        private val itemRepository: ItemRepository,
        private val orderRepository: OrderRepository,
        private val orderMapper: OrderMapper
) {

    fun createOrder(orderDTO: OrderDTO): OrderDTO {
        val customer =
                customerRepository.findById(orderDTO.customer.id!!).orElseThrow {
                    EntityNotFoundException("Customer not found with ID ${orderDTO.customer.id}")
                }

        val truck =
                truckRepository.findById(orderDTO.truck.id!!).orElseThrow {
                    EntityNotFoundException("Truck not found with ID ${orderDTO.truck.id}")
                }

        val orderEntity =
                Order(
                        id = null,
                        customer = customer,
                        items = orderDTO.items.map { itemRepository.findById(it.id!!).get() },
                        truck = truck,
                        date = LocalDate.now(),
                        totalPrice = orderDTO.items.sumOf { it.price * it.quantity },
                        status = OrderStatus.PENDING
                )

        return orderMapper.toDto(orderRepository.save(orderEntity))
    }

    fun getOrders(pageable: Pageable): Page<OrderDTO> {
        return orderRepository.findAll(pageable).map { orderMapper.toDto(it) }
    }

    fun getPendingOrders(): List<OrderDTO> {
        return orderRepository.findByStatus(OrderStatus.PENDING).map { orderMapper.toDto(it) }
    }

    fun getDeliveredOrders(): List<OrderDTO> {
        return orderRepository.findByStatus(OrderStatus.DELIVERED).map { orderMapper.toDto(it) }
    }

    fun markOrderAsDelivered(orderId: Int): OrderDTO {
        val order =
                orderRepository.findById(orderId).orElseThrow {
                    EntityNotFoundException("Order not found with ID $orderId")
                }

        val updatedOrder = order.copy(status = OrderStatus.DELIVERED)
        return orderMapper.toDto(orderRepository.save(updatedOrder))
    }

    fun getOrdersByCustomer(phoneNumber: String): List<Order> {
        return orderRepository.findByCustomerPhoneNumber(phoneNumber)
    }

    fun getOrdersByCustomerPaged(pageable: Pageable, phoneNumber: String): Page<OrderDTO> {
        return orderRepository.findByCustomerPhoneNumber(phoneNumber, pageable).map {
            orderMapper.toDto(it)
        }
    }

    fun getOrdersByTruckIdAndDate(
            truckId: Int,
            deliveryDate: LocalDate,
            pageable: Pageable
    ): Page<Order> {
        val endOfDay = deliveryDate.plusDays(1)
        return orderRepository.findByTruckIdAndDate(truckId, deliveryDate, endOfDay, pageable)
    }

    fun getTotalPriceByTruckIdAndDate(
            truckId: Int,
            deliveryDate: LocalDate,
            pageable: Pageable
    ): Double {
        return getOrdersByTruckIdAndDate(truckId, deliveryDate, pageable).content.sumOf {
            it.totalPrice
        }
    }

    fun assignTruckToOrder(orderId: Int, truckId: Int): OrderDTO {
        val order =
                orderRepository.findById(orderId).orElseThrow {
                    EntityNotFoundException("Order not found with ID $orderId")
                }

        val truck =
                truckRepository.findById(truckId).orElseThrow {
                    EntityNotFoundException("Truck not found with ID $truckId")
                }

        val updatedOrder = order.copy(truck = truck)
        return orderMapper.toDto(orderRepository.save(updatedOrder))
    }
}
