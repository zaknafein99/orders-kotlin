package com.kotlin.orders.controller

import com.kotlin.orders.dto.ItemDTO
import com.kotlin.orders.service.ItemService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/item")
class ItemController(val itemService: ItemService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addItem(@RequestBody itemDto: ItemDTO): ItemDTO {
        return itemService.addItem(itemDto)
    }

    @PutMapping("/{item_id}")
    @ResponseStatus(HttpStatus.OK)
    fun updateItem(@RequestBody itemDTO: ItemDTO, @PathVariable("item_id") itemId: Int) = itemService.updateItem(itemId, itemDTO)

    @DeleteMapping("/{item_id}")
    @ResponseStatus(HttpStatus.OK)
    fun deleteItem(@PathVariable("item_id") itemId: Int) = itemService.deleteItem(itemId)

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getItems(): List<ItemDTO> = itemService.getItems()

    @PostMapping("/list")
    @ResponseStatus(HttpStatus.CREATED)
    fun addListOfItems (@RequestBody itemDTO: List<ItemDTO>): List<ItemDTO> {
        return itemService.addListOfItems(itemDTO)
    }
}