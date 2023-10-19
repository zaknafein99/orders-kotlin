package com.kotlin.orders.controller

import com.kotlin.orders.dto.CustomerDTO
import com.kotlin.orders.service.CustomerService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = ["http://localhost:3000"])
@RestController
@RequestMapping("/customer")
@Validated
class CustomerController(val customerService : CustomerService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addCustomer(@RequestBody @Valid customerDTO: CustomerDTO): CustomerDTO {
        return customerService.addCustomer(customerDTO)
    }

    @PutMapping("/{course_id}")
    @ResponseStatus(HttpStatus.OK)
    fun updateCustomer(@RequestBody @Valid customerDTO: CustomerDTO, @PathVariable("course_id") courseId: Int) = customerService.updateCustomer(courseId, customerDTO)

    @DeleteMapping("/{course_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCustomer(@PathVariable("course_id") courseId: Int) = customerService.deleteCustomer(courseId)

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getCustomers(): List<CustomerDTO> = customerService.getCustomers()

    @PostMapping("/list")
    @ResponseStatus(HttpStatus.CREATED)
    fun addListOfCustomers (@RequestBody @Valid customerDTO: List<CustomerDTO>): List<CustomerDTO> {
        return customerService.addListOfCustomers(customerDTO)
    }

    @GetMapping("/search_phone/{phone_number}")
    @ResponseStatus(HttpStatus.OK)
    fun getCustomerByPhoneNumber(@PathVariable("phone_number") phoneNumber: String): List<CustomerDTO> {
        return customerService.getCustomerByPhoneNumber(phoneNumber)
    }
}