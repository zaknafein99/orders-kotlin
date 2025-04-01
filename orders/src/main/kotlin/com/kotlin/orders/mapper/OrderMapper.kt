package com.kotlin.orders.mapper

import com.kotlin.orders.dto.OrderDTO
import com.kotlin.orders.entity.Order
import org.mapstruct.*

@Mapper(
    componentModel = "spring",
    uses = [CustomerMapper::class, ItemMapper::class, TruckMapper::class],
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
interface OrderMapper {

    @Mapping(target = "customer", source = "customer")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "truck", source = "truck")
    fun toDto(order: Order): OrderDTO

    @Mapping(target = "customer", source = "customer")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "truck", source = "truck")
    fun toEntity(orderDTO: OrderDTO): Order

    fun toDtoList(orderList: List<Order>): List<OrderDTO>

    fun toEntityList(orderDTOList: List<OrderDTO>): List<Order>
}