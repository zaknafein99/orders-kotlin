package com.kotlin.orders.service

import com.kotlin.orders.dto.ItemDTO
import com.kotlin.orders.entity.Item
import com.kotlin.orders.exceptionhandler.ItemNotFoundException
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

        return itemRepository.findAll().map {
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

    fun updateItem(itemId: Int, itemDTO: ItemDTO): ItemDTO {

            val existingItem = itemRepository.findById(itemId)
            return if(existingItem.isPresent){
                existingItem.let {
                    it.get().name = itemDTO.name
                    it.get().description = itemDTO.description
                    it.get().price = itemDTO.price
                    it.get().quantity = itemDTO.quantity
                    it.get().category = itemDTO.category
                    itemRepository.save(it.get())
                    ItemDTO(it.get().id, it.get().name, it.get().description, it.get().price, it.get().quantity, it.get().category)
                }
            }else{
                throw ItemNotFoundException("Item not found with id $itemId")
            }

    }

    fun deleteItem(itemId: Int){
        val existingItem = itemRepository.findById(itemId)
         if(existingItem.isPresent){
            existingItem.let {
                itemRepository.delete(it.get())
            }
        }else{
            throw ItemNotFoundException("Item not found with id $itemId")
        }

    }

}
