package com.kotlin.orders.controller

import com.kotlin.orders.dto.TruckDTO
import com.kotlin.orders.service.TruckService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/truck")
class TruckController(val truckService: TruckService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addTruck(@RequestBody truckDTO: TruckDTO): TruckDTO {
        return truckService.addTruck(truckDTO)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getTrucks(): List<TruckDTO> = truckService.getTrucks()

}