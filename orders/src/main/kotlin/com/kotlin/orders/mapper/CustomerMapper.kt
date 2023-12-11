package com.kotlin.orders.mapper

import com.kotlin.orders.dto.CustomerDTO
import com.kotlin.orders.entity.Customer
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface CustomerMapper {

    fun customerDTOToCustomer(customerDTO: CustomerDTO): Customer

    fun customerToCustomerDTO(customer: Customer?): CustomerDTO

    fun customerDTOListToCustomerList(customerDTOList: List<CustomerDTO>): List<Customer>

    fun customerListToCustomerDTOList(customerList: List<Customer>): List<CustomerDTO>
}