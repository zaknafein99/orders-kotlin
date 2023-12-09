package com.kotlin.orders.controller

import com.kotlin.orders.dto.CustomerDTO
import com.kotlin.orders.repository.CustomerRepository
import com.kotlin.orders.service.CustomerService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*


@CrossOrigin
@RestController
@RequestMapping("/customer")
@Validated
class CustomerController(val customerService : CustomerService, val customerRepository: CustomerRepository) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addCustomer(@RequestBody @Valid customerDTO: CustomerDTO): CustomerDTO {
        return customerService.addCustomer(customerDTO)
    }

    @PutMapping("/{customer_id}")
    @ResponseStatus(HttpStatus.OK)
    fun updateCustomer(@RequestBody @Valid customerDTO: CustomerDTO, @PathVariable("customer_id") customerId: Int) = customerService.updateCustomer(customerId, customerDTO)

    @DeleteMapping("/{customer_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCustomer(@PathVariable("customer_id") customerId: Int) = customerService.deleteCustomer(customerId)

    @GetMapping("/list")
    fun getCustomersPaged(@PageableDefault(page = 0, size = 10) pageable: Pageable) : Page<CustomerDTO> {
        return customerService.getCustomersPaged(pageable)
    }


    @PostMapping("/list")
    @ResponseStatus(HttpStatus.CREATED)
    fun addListOfCustomers (@RequestBody @Valid customerDTO: List<CustomerDTO>): List<CustomerDTO> {
        return customerService.addListOfCustomers(customerDTO)
    }

    @GetMapping("/search_phone/{phone_number}")
    @ResponseStatus(HttpStatus.OK)
    fun getCustomerByPhoneNumber(@PathVariable("phone_number") phoneNumber: String, pageable: Pageable): Page<CustomerDTO> {
        return customerService.getCustomerByPhoneNumber(phoneNumber, pageable)
    }
}