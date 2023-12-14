package com.kotlin.orders.mapper

import com.kotlin.orders.dto.OrderDTO
import com.kotlin.orders.entity.Order
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface OrderMapper {

    fun toDto(order: Order): OrderDTO

    fun toEntity(orderDTO: OrderDTO): Order

    fun toDtoList(orderList: List<Order>): List<OrderDTO>

    fun toEntityList(orderDTOList: List<OrderDTO>): List<Order>

}