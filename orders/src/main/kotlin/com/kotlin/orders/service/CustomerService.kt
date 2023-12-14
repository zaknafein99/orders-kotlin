package com.kotlin.orders.service

import com.kotlin.orders.dto.CustomerDTO
import com.kotlin.orders.exceptionhandler.CustomerNotFoundException
import com.kotlin.orders.mapper.CustomerMapper
import com.kotlin.orders.repository.CustomerRepository
import mu.KLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service


@Service
class CustomerService(val customerRepository: CustomerRepository, val customerMapper: CustomerMapper) {

    companion object : KLogging()

    fun addCustomer(customerDTO: CustomerDTO): CustomerDTO {

        val customerEntity = customerMapper.customerDTOToCustomer(customerDTO)
        customerRepository.save(customerEntity)

        logger.info { "Customer added: $customerEntity" }

        return customerMapper.customerToCustomerDTO(customerEntity)
    }

    fun addListOfCustomers(customerDTO: List<CustomerDTO>): List<CustomerDTO> {

        val customerEntity = customerMapper.customerDTOListToCustomerList(customerDTO)
        customerRepository.saveAll(customerEntity)
        logger.info { "Customer added: $customerEntity" }

        return customerMapper.customerListToCustomerDTOList(customerEntity)
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
        val existingCustomersDto = customerMapper.customerListToCustomerDTOList(existingCustomer)
        return PageImpl(existingCustomersDto, pageable, existingCustomersDto.size.toLong())
    }

    fun getCustomerById(customerId: Int): CustomerDTO {

        val existingCustomer = customerRepository.findById(customerId)
        return if (existingCustomer.isPresent) {
            customerMapper.customerToCustomerDTO(existingCustomer.get())

        } else {
            throw CustomerNotFoundException("Customer not found with id: $customerId")
        }
    }

    fun getCustomersPaged(pageable: Pageable): Page<CustomerDTO> {
        return customerRepository.findAll(pageable).map { customerMapper.customerToCustomerDTO(it) }
    }
}

