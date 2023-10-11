package com.kotlin.orders.controller

import com.kotlin.orders.dto.ItemDTO
import com.kotlin.orders.service.ItemService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/item")
@Validated
class ItemController(val itemService: ItemService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addItem(@RequestBody @Valid itemDto: ItemDTO): ItemDTO {
        return itemService.addItem(itemDto)
    }

    @PutMapping("/{item_id}")
    @ResponseStatus(HttpStatus.OK)
    fun updateItem(@RequestBody itemDTO: ItemDTO, @PathVariable("item_id") itemId: Int) = itemService.updateItem(itemId, itemDTO)

    @DeleteMapping("/{item_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteItem(@PathVariable("item_id") itemId: Int) = itemService.deleteItem(itemId)

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getItems(): List<ItemDTO> = itemService.getItems()

    @PostMapping("/list")
    @ResponseStatus(HttpStatus.CREATED)
    fun addListOfItems (@RequestBody @Valid itemDTO: List<ItemDTO>): List<ItemDTO> {
        return itemService.addListOfItems(itemDTO)
    }
}