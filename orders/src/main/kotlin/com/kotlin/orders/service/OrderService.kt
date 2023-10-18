package com.kotlin.orders.service

import com.kotlin.orders.dto.OrderDTO
import com.kotlin.orders.dto.ItemDTO
import com.kotlin.orders.dto.TruckDTO
import com.kotlin.orders.dto.CustomerDTO
import com.kotlin.orders.entity.Order
import com.kotlin.orders.repository.CustomerRepository
import com.kotlin.orders.repository.ItemRepository
import com.kotlin.orders.repository.OrderRepository
import com.kotlin.orders.repository.TruckRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class OrderService(val customerRepository : CustomerRepository,
                   val truckRepository : TruckRepository,
                   val itemRepository : ItemRepository,
                   val orderRepository : OrderRepository
) {

    fun createOrder(orderDTO: OrderDTO): OrderDTO {
        val customer = orderDTO.customer.id?.let {
            customerRepository.findById(it)
                .orElseThrow { EntityNotFoundException("Customer not found with ID ${orderDTO.customer}") }
        }

        val truck = orderDTO.truck.id?.let {
            truckRepository.findById(it)
                .orElseThrow { EntityNotFoundException("Truck not found with ID ${orderDTO.truck}") }
        }

        val items = orderDTO.items.map {
            itemRepository.findById(it.id!!)
                .orElseThrow { EntityNotFoundException("Item not found with ID ${it.id}") }
        }

        val orderEntity = Order(
            id = null,
            customer = customer!!,
            items = items,
            truck = truck!!,
            date = LocalDateTime.now(),
            totalPrice = items.sumOf { it.price * it.quantity }
        )

        orderRepository.save(orderEntity)

        return OrderDTO(
            id = orderEntity.id,
            customer = orderDTO.customer,
            items = orderDTO.items,
            truck = orderDTO.truck,
            date = orderDTO.date,
            totalPrice = orderDTO.totalPrice
        )
    }

    fun getOrders(): List<OrderDTO> {
        val orders = orderRepository.findAll()
        
        return orders.map { order ->
            OrderDTO(
                id = order.id,
                customer = CustomerDTO(
                    id = order.customer.id,
                    name = order.customer.name,
                    address = order.customer.address,
                    phoneNumber = order.customer.phoneNumber,
                ),
                items = order.items.map { item ->
                    ItemDTO(
                        id = item.id,
                        name = item.name,
                        quantity = item.quantity,
                        price = item.price,
                        category = item.category,
                        description = item.description
                    )
                },
                truck = TruckDTO(
                    id = order.truck.id,
                    name = order.truck.name
                ),
                date = order.date,
                totalPrice = order.totalPrice
            )
        }
    }

    fun getOrdersByCustomer(phoneNumber: String): List<OrderDTO> {
        val orders = orderRepository.findAll()
    
        return orders.filter { it.customer.phoneNumber == phoneNumber }.map { order ->
            OrderDTO(
                id = order.id,
                customer = CustomerDTO(
                    id = order.customer.id,
                    name = order.customer.name,
                    address = order.customer.address,
                    phoneNumber = order.customer.phoneNumber,
                ),
                items = order.items.map { item ->
                    ItemDTO(
                        id = item.id,
                        name = item.name,
                        quantity = item.quantity,
                        price = item.price,
                        category = item.category,
                        description = item.description
                    )
                },
                truck = TruckDTO(
                    id = order.truck.id,
                    name = order.truck.name
                ),
                date = order.date,
                totalPrice = order.totalPrice
            )
        }
    }
}

