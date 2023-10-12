package com.kotlin.orders.service

import com.kotlin.orders.dto.CustomerDTO
import com.kotlin.orders.entity.Customer
import com.kotlin.orders.exceptionhandler.CustomerNotFoundException
import com.kotlin.orders.repository.CustomerRepository
import mu.KLogging
import org.springframework.stereotype.Service

@Service
class CustomerService(val customerRepository: CustomerRepository) {

    companion object : KLogging()

    fun addCustomer(customerDTO: CustomerDTO): CustomerDTO {

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
        return customerRepository.findAll().map {
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

    fun updateCustomer(courseId: Int, customerDTO: CustomerDTO): CustomerDTO {

        val existingCustomer = customerRepository.findById(courseId)
        return if (existingCustomer.isPresent) {
            existingCustomer.let {
                it.get().name = customerDTO.name
                it.get().address = customerDTO.address
                it.get().phoneNumber = customerDTO.phoneNumber
                customerRepository.save(it.get())
                CustomerDTO(it.get().id, it.get().name, it.get().address, it.get().phoneNumber)
            }
        } else {
            throw CustomerNotFoundException("Customer not found with id: $courseId")
        }
    }

    fun deleteCustomer(courseId: Int) {
        val existingCustomer = customerRepository.findById(courseId)
        if (existingCustomer.isPresent) {
            existingCustomer.let {
                customerRepository.delete(it.get())
            }
        } else {
            throw CustomerNotFoundException("Customer not found with id: $courseId")
        }
    }

    fun getCustomerByPhoneNumber(phoneNumber: String): List<CustomerDTO> {

        val existingCustomer = customerRepository.findByPhoneNumber(phoneNumber)
        return if (existingCustomer.isNotEmpty()) {
            existingCustomer.map {
                CustomerDTO(it!!.id, it.name, it.address, it.phoneNumber)
            }
        } else {
            throw CustomerNotFoundException("Customer not found with phone number: $phoneNumber")
        }
    }
}

