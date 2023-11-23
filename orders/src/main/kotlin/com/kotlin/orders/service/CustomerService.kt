package com.kotlin.orders.service

import com.kotlin.orders.dto.CustomerDTO
import com.kotlin.orders.entity.Customer
import com.kotlin.orders.exceptionhandler.CustomerNotFoundException
import com.kotlin.orders.repository.CustomerRepository
import mu.KLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service


@Service
class CustomerService(val customerRepository: CustomerRepository) {

    companion object : KLogging()

    fun addCustomer(customerDTO: CustomerDTO): CustomerDTO {

        val customerEntity = customerDTO.let {
            Customer(null, it.name, it.address, it.phoneNumber, it.type, it.state, emptyList())
        }
        customerRepository.save(customerEntity)

        logger.info { "Customer added: $customerEntity" }

        return customerEntity.let {
            CustomerDTO(it.id, it.name, it.address, it.phoneNumber, it.type, it.state)
        }
    }

    fun addListOfCustomers(customerDTO: List<CustomerDTO>): List<CustomerDTO> {

        val customerEntity = customerDTO.map {
            Customer(null, it.name, it.address, it.phoneNumber, it.type, it.state, emptyList())
        }
        customerRepository.saveAll(customerEntity)
        logger.info { "Customer added: $customerEntity" }

        return customerEntity.map {
            CustomerDTO(it.id, it.name, it.address, it.phoneNumber, it.type, it.state)
        }

    }

    fun updateCustomer(customerId: Int, customerDTO: CustomerDTO): CustomerDTO {

        val existingCustomer = customerRepository.findById(customerId)
        return if (existingCustomer.isPresent) {
            existingCustomer.let {
                it.get().name = customerDTO.name
                it.get().address = customerDTO.address
                it.get().phoneNumber = customerDTO.phoneNumber
                customerRepository.save(it.get())
                CustomerDTO(it.get().id, it.get().name, it.get().address, it.get().phoneNumber, it.get().type, it.get().state)
            }
        } else {
            throw CustomerNotFoundException("Customer not found with id: $customerId")
        }
    }

    fun deleteCustomer(customerId: Int) {
        val existingCustomer = customerRepository.findById(customerId)
        if (existingCustomer.isPresent) {
            existingCustomer.let {
                customerRepository.delete(it.get())
            }
        } else {
            throw CustomerNotFoundException("Customer not found with id: $customerId")
        }
    }

    fun getCustomerByPhoneNumber(phoneNumber: String, pageable: Pageable): Page<CustomerDTO> {
        val existingCustomer = customerRepository.findByPhoneNumber(phoneNumber)
        val customerList = if (existingCustomer.isNotEmpty()) {
            existingCustomer.map {
                CustomerDTO(it!!.id, it.name, it.address, it.phoneNumber, it.type, it.state)
            }
        } else {
            throw CustomerNotFoundException("Customer not found with phone number: $phoneNumber")
        }
        return PageImpl(customerList, pageable, customerList.size.toLong())
    }

    fun getCustomerById(customerId: Int): CustomerDTO {

        val existingCustomer = customerRepository.findById(customerId)
        return if (existingCustomer.isPresent) {
            existingCustomer.let {
                CustomerDTO(it.get().id, it.get().name, it.get().address, it.get().phoneNumber, it.get().type, it.get().state)
            }
        } else {
            throw CustomerNotFoundException("Customer not found with id: $customerId")
        }
    }

    fun getCustomersPaged(pageable: Pageable): Page<Customer> {
        return customerRepository.findAll(pageable)
    }
}

