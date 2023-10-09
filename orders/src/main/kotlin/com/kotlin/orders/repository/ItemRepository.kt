package com.kotlin.orders.repository

import com.kotlin.orders.entity.Item
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ItemRepository : CrudRepository<Item, Int> {

}
