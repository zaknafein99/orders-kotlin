package com.kotlin.orders.service

import com.kotlin.orders.dto.CustomerDTO
import com.kotlin.orders.entity.Customer
import com.kotlin.orders.repository.CustomerRepository
import mu.KLogging
import org.springframework.stereotype.Service

@Service
class CustomerService(val customerRepository: CustomerRepository) {

    companion object : KLogging()

    fun addCustomer(customerDTO: CustomerDTO) : CustomerDTO {

        val customerEntity = customerDTO.let {
            Customer(null, it.name, it.address, it.phoneNumber)
                }
        customerRepository.save(customerEntity)

        logger.info { "Customer added: $customerEntity" }

        return customerEntity.let {
            CustomerDTO(it.id, it.name, it.address, it.phoneNumber)
        }
    }

    fun getCustomers(): List<CustomerDTO> {
        val customers = customerRepository.findAll()
        return customers.map {
            CustomerDTO(it.id, it.name, it.address, it.phoneNumber)
        }
    }

    fun addListOfCustomers(customerDTO: List<CustomerDTO>): List<CustomerDTO> {

        val customerEntity = customerDTO.map {
            Customer(null, it.name, it.address, it.phoneNumber)
        }
        customerRepository.saveAll(customerEntity)
        logger.info { "Customer added: $customerEntity" }

        return customerEntity.map {
            CustomerDTO(it.id, it.name, it.address, it.phoneNumber)
        }

    }
}

