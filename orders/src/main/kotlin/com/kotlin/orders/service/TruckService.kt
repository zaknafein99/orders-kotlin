package com.kotlin.orders.service

import com.kotlin.orders.dto.TruckDTO
import com.kotlin.orders.entity.Truck
import com.kotlin.orders.repository.TruckRepository
import mu.KLogging
import org.springframework.stereotype.Service

@Service
class TruckService(val truckRepository: TruckRepository){

    companion object : KLogging()

    fun addTruck(truckDTO: TruckDTO): TruckDTO {
        val truckEntity = Truck(null, truckDTO.name)
        truckRepository.save(truckEntity)
        logger.info { "Truck added: $truckEntity" }

        return truckEntity.let {
            TruckDTO(it.id, it.name)
        }

    }

    fun getTrucks(): List<TruckDTO> {

        val trucks = truckRepository.findAll()
        return trucks.map {
            TruckDTO(it.id, it.name)
        }

    }

}
