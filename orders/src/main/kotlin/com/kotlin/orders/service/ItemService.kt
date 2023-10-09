package com.kotlin.orders.service

import com.kotlin.orders.dto.ItemDTO
import com.kotlin.orders.entity.Item
import com.kotlin.orders.repository.ItemRepository
import mu.KLogging
import org.springframework.stereotype.Service

@Service
class ItemService(val itemRepository: ItemRepository){

    companion object : KLogging()

    fun addItem(itemDto: ItemDTO): ItemDTO {
        val itemEntity = itemDto.let {
            Item(null, it.name, it.description, it.price, it.quantity, it.category)
        }
        itemRepository.save(itemEntity)
        logger.info { "Item added: $itemEntity" }

        return itemEntity.let {
            ItemDTO(it.id, it.name, it.description, it.price, it.quantity, it.category)
        }

    }

    fun getItems(): List<ItemDTO> {

        val items = itemRepository.findAll()
        return items.map {
            ItemDTO(it.id, it.name, it.description, it.price, it.quantity, it.category)
        }

    }

    fun addListOfItems(itemDTO: List<ItemDTO>): List<ItemDTO> {

        val itemEntity = itemDTO.map {
            Item(null, it.name, it.description, it.price, it.quantity, it.category)
        }
        itemRepository.saveAll(itemEntity)
        logger.info { "Item added: $itemEntity" }

        return itemEntity.map {
            ItemDTO(it.id, it.name, it.description, it.price, it.quantity, it.category)
        }

    }

}
