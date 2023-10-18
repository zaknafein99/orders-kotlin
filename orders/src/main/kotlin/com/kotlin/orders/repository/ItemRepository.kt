package com.kotlin.orders.repository

import com.kotlin.orders.entity.Item
import org.hibernate.cache.spi.support.AbstractReadWriteAccess
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ItemRepository : CrudRepository<Item, Int> {
    override fun findById(id: Int): Optional<Item>

}
