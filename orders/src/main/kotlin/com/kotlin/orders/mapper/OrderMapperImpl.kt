package com.kotlin.orders.mapper

import com.kotlin.orders.dto.OrderDTO
import com.kotlin.orders.entity.Order
import com.kotlin.orders.repository.CustomerRepository
import com.kotlin.orders.repository.ItemRepository
import com.kotlin.orders.repository.TruckRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Component

@Component
class OrderMapperImpl(  private val customerRepository: CustomerRepository,
                        private val itemRepository: ItemRepository,
                        private val truckRepository: TruckRepository,
                        private val customerMapper: CustomerMapper
) {

    fun toDto(order: Order): OrderDTO {
        return OrderDTO(
            id = order.id,
            customer = customerMapper.toDto(order.customer),

            items = order.items.map { it.id!! },
            truckId = order.truck.id!!,
            date = order.date,
            totalPrice = order.totalPrice
        )
    }

    fun toEntity(orderDTO: OrderDTO): Order {
        return Order(
            id = orderDTO.id,
            customer = customerRepository.findById(orderDTO.customer.id!!)
                .orElseThrow { EntityNotFoundException("Customer not found with ID ${orderDTO.customer.id}") },
            items = orderDTO.items.map { itemRepository.findById(it).get() },
            truck = truckRepository.findById(orderDTO.truckId)
                .orElseThrow { EntityNotFoundException("Truck not found with ID ${orderDTO.truckId}") },
            date = orderDTO.date,
            totalPrice = orderDTO.totalPrice
        )
    }
}
