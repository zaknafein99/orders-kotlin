package com.kotlin.orders.mapper

import com.kotlin.orders.dto.CustomerDTO
import com.kotlin.orders.entity.Customer
import org.springframework.stereotype.Component

@Component
class CustomerMapper {

    fun toDto(customer: Customer): CustomerDTO {
        return CustomerDTO(
            id = customer.id,
            name = customer.name,
            phoneNumber = customer.phoneNumber,
            address = customer.address,
            state = customer.state,
            type = customer.type
        )
    }

    fun toEntity(customerDTO: CustomerDTO): Customer {
        return Customer(
            id = customerDTO.id,
            name = customerDTO.name,
            phoneNumber = customerDTO.phoneNumber,
            address = customerDTO.address,
            state = customerDTO.state,
            type = customerDTO.type,
            orders = mutableListOf()
        )
    }
}